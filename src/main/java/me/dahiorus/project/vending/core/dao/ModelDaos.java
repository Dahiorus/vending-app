package me.dahiorus.project.vending.core.dao;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import me.dahiorus.project.vending.core.model.Comment;
import me.dahiorus.project.vending.core.model.CommunicationModule;
import me.dahiorus.project.vending.core.model.Item;
import me.dahiorus.project.vending.core.model.Report;
import me.dahiorus.project.vending.core.model.Stock;
import me.dahiorus.project.vending.core.model.VendingMachine;

public class ModelDaos
{
  private ModelDaos()
  {
    // Factory class
  }

  @Repository
  public static class VendingMachineDAO extends AbstractDAO<VendingMachine>
  {
    public VendingMachineDAO(final EntityManager em)
    {
      super(VendingMachine.class, em);
    }
  }

  @Repository
  public static class ItemDAO extends AbstractDAO<Item>
  {
    public ItemDAO(final EntityManager em)
    {
      super(Item.class, em);
    }
  }

  @Repository
  public static class CommentDAO extends AbstractDAO<Comment>
  {
    public CommentDAO(final EntityManager em)
    {
      super(Comment.class, em);
    }
  }

  @Repository
  public static class CommunicationModuleDAO extends AbstractDAO<CommunicationModule>
  {
    public CommunicationModuleDAO(final EntityManager em)
    {
      super(CommunicationModule.class, em);
    }
  }

  @Repository
  public static class StockDAO extends AbstractDAO<Stock>
  {
    public StockDAO(final EntityManager em)
    {
      super(Stock.class, em);
    }
  }

  @Repository
  public static class ReportDAO extends AbstractDAO<Report>
  {
    public ReportDAO(final EntityManager em)
    {
      super(Report.class, em);
    }
  }
}
