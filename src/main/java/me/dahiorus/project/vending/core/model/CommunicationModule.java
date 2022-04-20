package me.dahiorus.project.vending.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "communication_module")
public class CommunicationModule extends AbstractEntity
{
  private MessageFormat format;

  private String url;

  private List<VendingMachine> machines = new ArrayList<>(0);

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public MessageFormat getFormat()
  {
    return format;
  }

  public void setFormat(final MessageFormat format)
  {
    this.format = format;
  }

  @Column(length = 255, nullable = false)
  public String getUrl()
  {
    return url;
  }

  public void setUrl(final String url)
  {
    this.url = url;
  }

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "module")
  public List<VendingMachine> getMachines()
  {
    return machines;
  }

  public void setMachines(final List<VendingMachine> machines)
  {
    this.machines = machines;
  }

  @Override
  public String toString()
  {
    return super.toString() + "[format=" + format + ", url=" + url + "]";
  }
}
