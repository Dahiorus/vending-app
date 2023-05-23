package me.dahiorus.project.vending.domain.model.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.dahiorus.project.vending.domain.model.Item;
import me.dahiorus.project.vending.domain.model.ItemType;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ItemDTO extends AbstractDTO<Item>
{
  @Getter
  @Setter
  private String name;

  @Getter
  @Setter
  private ItemType type;

  @Getter
  @Setter
  private BigDecimal price;

  @JsonIgnore
  @Getter
  @Setter
  private UUID pictureId;

  @Override
  public Class<Item> getEntityClass()
  {
    return Item.class;
  }
}
