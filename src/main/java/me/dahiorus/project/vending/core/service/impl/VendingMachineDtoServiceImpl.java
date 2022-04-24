package me.dahiorus.project.vending.core.service.impl;

import static me.dahiorus.project.vending.core.service.validation.ValidationError.objectError;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ItemMissing;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.manager.impl.VendingMachineManager;
import me.dahiorus.project.vending.core.model.Comment;
import me.dahiorus.project.vending.core.model.Item;
import me.dahiorus.project.vending.core.model.Sale;
import me.dahiorus.project.vending.core.model.Stock;
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
    extends DtoServiceImpl<VendingMachine, VendingMachineDTO, VendingMachineManager>
    implements VendingMachineDtoService
{
  private static final Logger logger = LogManager.getLogger(VendingMachineDtoServiceImpl.class);

  private final DtoValidator<Stock, StockDTO> stockDtoValidator;

  private final DtoValidator<Comment, CommentDTO> commentDtoValidator;

  @Autowired
  public VendingMachineDtoServiceImpl(final VendingMachineManager manager, final DtoMapper dtoMapper,
      final DtoValidator<VendingMachine, VendingMachineDTO> dtoValidator,
      final DtoValidator<Stock, StockDTO> stockDtoValidator,
      final DtoValidator<Comment, CommentDTO> commentDtoValidator)
  {
    super(manager, dtoMapper, dtoValidator);
    this.stockDtoValidator = stockDtoValidator;
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
    VendingMachine entity = manager.read(id);
    List<StockDTO> stocks = entity.getStocks()
      .stream()
      .map(stock -> dtoMapper.toDto(stock, StockDTO.class))
      .toList();
    stocks.forEach(stock -> stock.setVendingMachineId(id));

    return stocks;
  }

  @Transactional(rollbackFor = { EntityNotFound.class, ValidationException.class })
  @Override
  public void provisionStock(final UUID id, final ItemDTO itemDto, final Long quantity)
      throws EntityNotFound, ValidationException
  {
    logger.traceEntry(() -> id, () -> itemDto, () -> quantity);

    VendingMachine machine = manager.read(id);
    StockDTO stock = new StockDTO();
    stock.setItemId(itemDto.getId());
    stock.setItemName(itemDto.getName());
    stock.setQuantity(quantity);

    ValidationResults validationResults = stockDtoValidator.validate(stock);
    // the item type must be the same as the machine type
    if (machine.getType() != itemDto.getType())
    {
      validationResults.addError(objectError("validation.constraints.stock.invalid_item",
          "Unable to add a stock of " + itemDto.getName() + " in the machine " + machine.getId(), itemDto.getName(),
          machine.getId()));
    }
    validationResults.throwIfError(stock, CrudOperation.UPDATE);

    Item item = dtoMapper.toEntity(itemDto, Item.class);
    if (!machine.hasItem(item))
    {
      logger.debug("Adding a new stock of {} in machine {}", stock, machine);

      // no stock exists with the given item, so we add a new stock with this item
      Stock stockToAdd = dtoMapper.toEntity(stock, Stock.class);
      machine.addStock(stockToAdd);
    }
    else
    {
      // a stock already exists (even if empty), so we add the adequate quantity
      machine.getStocks()
        .stream()
        .filter(s -> Objects.equals(s.getItem(), item))
        .findFirst()
        .ifPresent(s -> {
          getLogger().debug("Refill existing stock of {} in machine {}", stock, machine);
          s.addQuantity(stock.getQuantity());
        });
    }

    machine.setLastIntervention(Instant.now());
    VendingMachine updatedMachine = manager.update(machine);

    logger.info("Vending machine {} stock of {} provisioned with {}", updatedMachine.getId(), itemDto, stock);
  }

  @Transactional(rollbackFor = { EntityNotFound.class, ItemMissing.class })
  @Override
  public SaleDTO purchaseItem(final UUID id, final ItemDTO item) throws EntityNotFound, ItemMissing
  {
    logger.traceEntry(() -> id, () -> item);

    VendingMachine machine = manager.read(id);
    Item entity = dtoMapper.toEntity(item, Item.class);

    if (!machine.hasItem(entity) || machine.getQuantityInStock(entity) == 0)
    {
      throw new ItemMissing("Vending machine " + machine.getId() + " does not have item " + item.getName());
    }

    // decrement machine stock
    machine.getStocks()
      .stream()
      .filter(stock -> Objects.equals(stock.getItem(), entity))
      .findFirst()
      .ifPresent(stock -> {
        logger.debug("Removing one item {} from the stock {}", entity.getName(), stock);
        stock.decrementQuantity();
      });

    // add a new sale to the machine
    Sale sale = new Sale();
    sale.setId(UUID.randomUUID());
    sale.setAmount(item.getPrice());
    sale.setMachine(machine);

    logger.debug("Adding new sale {} to vending machine {}", sale, machine.getId());

    machine.addSale(sale);
    VendingMachine updatedMachine = manager.update(machine);

    logger.info("Item {} purchased from vending machine {}: {}", item.getName(), updatedMachine.getId(), sale);

    return logger.traceExit(dtoMapper.toDto(sale, SaleDTO.class));
  }

  @Transactional(readOnly = true, rollbackFor = EntityNotFound.class)
  @Override
  public List<CommentDTO> getComments(final UUID id) throws EntityNotFound
  {
    VendingMachine entity = manager.read(id);
    List<CommentDTO> comments = entity.getComments()
      .stream()
      .map(comment -> dtoMapper.toDto(comment, CommentDTO.class))
      .toList();
    comments.forEach(comment -> comment.setVendingMachineId(id));

    return comments;
  }

  @Transactional(rollbackFor = { EntityNotFound.class, ValidationException.class })
  @Override
  public void comment(final UUID id, final CommentDTO commentDto) throws EntityNotFound, ValidationException
  {
    logger.traceEntry(() -> id, () -> commentDto);

    VendingMachine machine = manager.read(id);

    ValidationResults validationResults = commentDtoValidator.validate(commentDto);
    validationResults.throwIfError(commentDto, CrudOperation.CREATE);

    Comment comment = dtoMapper.toEntity(commentDto, Comment.class);
    machine.addComment(comment);
    manager.update(machine);

    logger.info("Comment {} added to vending machine {}", commentDto, machine.getId());
  }
}
