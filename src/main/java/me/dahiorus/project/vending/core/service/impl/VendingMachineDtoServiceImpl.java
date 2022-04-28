package me.dahiorus.project.vending.core.service.impl;

import static me.dahiorus.project.vending.core.service.validation.FieldValidationError.fieldError;
import static me.dahiorus.project.vending.core.service.validation.ValidationError.objectError;

import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.dahiorus.project.vending.core.dao.VendingMachineDAO;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ItemMissing;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.Comment;
import me.dahiorus.project.vending.core.model.Item;
import me.dahiorus.project.vending.core.model.Sale;
import me.dahiorus.project.vending.core.model.VendingMachine;
import me.dahiorus.project.vending.core.model.dto.CommentDTO;
import me.dahiorus.project.vending.core.model.dto.ItemDTO;
import me.dahiorus.project.vending.core.model.dto.SaleDTO;
import me.dahiorus.project.vending.core.model.dto.StockDTO;
import me.dahiorus.project.vending.core.model.dto.VendingMachineDTO;
import me.dahiorus.project.vending.core.service.DtoMapper;
import me.dahiorus.project.vending.core.service.VendingMachineDtoService;
import me.dahiorus.project.vending.core.service.validation.CrudOperation;
import me.dahiorus.project.vending.core.service.validation.DtoValidator;
import me.dahiorus.project.vending.core.service.validation.ValidationResults;

@Service
public class VendingMachineDtoServiceImpl
    extends DtoServiceImpl<VendingMachine, VendingMachineDTO, VendingMachineDAO>
    implements VendingMachineDtoService
{
  private static final Logger logger = LogManager.getLogger(VendingMachineDtoServiceImpl.class);

  private final DtoValidator<CommentDTO> commentDtoValidator;

  public VendingMachineDtoServiceImpl(final VendingMachineDAO dao, final DtoMapper dtoMapper,
      final DtoValidator<VendingMachineDTO> dtoValidator,
      final DtoValidator<CommentDTO> commentDtoValidator)
  {
    super(dao, dtoMapper, dtoValidator);
    this.commentDtoValidator = commentDtoValidator;
  }

  @Override
  public Logger getLogger()
  {
    return logger;
  }

  @Override
  protected Class<VendingMachineDTO> getDomainClass()
  {
    return VendingMachineDTO.class;
  }

  @Transactional(rollbackFor = EntityNotFound.class, readOnly = true)
  @Override
  public List<StockDTO> getStocks(final UUID id) throws EntityNotFound
  {
    VendingMachine entity = dao.read(id);
    List<StockDTO> stocks = entity.getStocks()
      .stream()
      .map(stock -> dtoMapper.toDto(stock, StockDTO.class))
      .toList();
    stocks.forEach(stock -> stock.setVendingMachineId(id));

    return stocks;
  }

  @Transactional(rollbackFor = { EntityNotFound.class, ValidationException.class })
  @Override
  public void provisionStock(final UUID id, final ItemDTO item, final Integer quantity)
      throws EntityNotFound, ValidationException
  {
    logger.traceEntry(() -> id, () -> item, () -> quantity);

    VendingMachine machine = dao.read(id);

    ValidationResults validationResults = validateStock(item, quantity, machine);
    validationResults.throwIfError("Cannot provision " + item + " to vending machine " + id);

    logger.debug("Provisioning {} to vending machine {}", item, id);

    Item itemToProvision = dtoMapper.toEntity(item, Item.class);
    machine.provision(itemToProvision, quantity);
    machine.markIntervention();

    VendingMachine updatedMachine = dao.save(machine);

    logger.info("Vending machine {} stock of {} provisioned with {}", updatedMachine.getId(), item, quantity);
  }

  private static ValidationResults validateStock(final ItemDTO itemDto, final Integer quantity,
      final VendingMachine machine)
  {
    ValidationResults validationResults = new ValidationResults();

    // the item type must be the same as the machine type
    if (machine.getType() != itemDto.getType())
    {
      validationResults.addError(objectError("validation.constraints.stock.invalid_item",
          "Unable to add a stock of " + itemDto.getName() + " in the machine " + machine.getId(), itemDto.getName(),
          machine.getId()));
    }

    if (quantity == null || quantity < 1L)
    {
      validationResults.addError(fieldError("quantity", "validation.constraints.stock.quantity_positive",
          "The quantity to provision must be positive", quantity));
    }

    return validationResults;
  }

  @Transactional(rollbackFor = { EntityNotFound.class, ItemMissing.class })
  @Override
  public SaleDTO purchaseItem(final UUID id, final ItemDTO item) throws EntityNotFound, ItemMissing
  {
    logger.traceEntry(() -> id, () -> item);

    VendingMachine machine = dao.read(id);
    Item itemToPurchase = dtoMapper.toEntity(item, Item.class);

    if (!machine.hasItem(itemToPurchase) || machine.getQuantityInStock(itemToPurchase) == 0L)
    {
      throw new ItemMissing("Vending machine " + id + " does not have item " + item.getName());
    }

    logger.debug("Purchasing {} from the vending machine {}", item.getName(), id);
    Sale sale = machine.purchase(itemToPurchase);
    VendingMachine updatedMachine = dao.save(machine);

    logger.info("Item {} purchased from vending machine {} for price {}", item.getName(), updatedMachine.getId(),
        sale.getAmount());

    return logger.traceExit(dtoMapper.toDto(sale, SaleDTO.class));
  }

  @Transactional(readOnly = true, rollbackFor = EntityNotFound.class)
  @Override
  public List<CommentDTO> getComments(final UUID id) throws EntityNotFound
  {
    VendingMachine entity = dao.read(id);
    List<CommentDTO> comments = entity.getComments()
      .stream()
      .map(comment -> dtoMapper.toDto(comment, CommentDTO.class))
      .toList();
    comments.forEach(comment -> comment.setVendingMachineId(id));

    return comments;
  }

  @Transactional(rollbackFor = { EntityNotFound.class, ValidationException.class })
  @Override
  public void comment(final UUID id, final CommentDTO comment) throws EntityNotFound, ValidationException
  {
    logger.traceEntry(() -> id, () -> comment);

    VendingMachine machine = dao.read(id);

    ValidationResults validationResults = commentDtoValidator.validate(comment);
    validationResults.throwIfError(comment, CrudOperation.CREATE);

    Comment commentToAdd = dtoMapper.toEntity(comment, Comment.class);
    machine.addComment(commentToAdd);
    dao.save(machine);

    logger.info("Comment {} added to vending machine {}", comment, machine.getId());
  }
}
