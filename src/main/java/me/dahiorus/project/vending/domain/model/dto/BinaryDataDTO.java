package me.dahiorus.project.vending.domain.model.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.dahiorus.project.vending.domain.model.BinaryData;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, of = { "name", "contentType" })
public class BinaryDataDTO extends AbstractDTO<BinaryData>
{
  @Getter
  @Setter
  private String name;

  @Getter
  @Setter
  private String contentType;

  @Getter
  @Setter
  private byte[] content;

  public int getSize()
  {
    return content.length;
  }

  @Override
  public Class<BinaryData> getEntityClass()
  {
    return BinaryData.class;
  }
}
