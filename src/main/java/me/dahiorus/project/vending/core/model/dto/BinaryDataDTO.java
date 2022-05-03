package me.dahiorus.project.vending.core.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.dahiorus.project.vending.core.model.BinaryData;

@ToString(callSuper = true, of = { "name", "size", "contentType" })
public class BinaryDataDTO extends AbstractDTO<BinaryData>
{
  @Getter
  @Setter
  private String name;

  @Getter
  @Setter
  private Long size;

  @Getter
  @Setter
  private String contentType;

  @Getter
  @Setter
  private byte[] content;

  @Override
  public Class<BinaryData> getEntityClass()
  {
    return BinaryData.class;
  }
}
