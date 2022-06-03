package org.exoplatform.dlp.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.dlp.domain.DlpOperation;

public class DlpOperationDAOTest {

  private DlpOperationDAO dlpOperationDAO;

  @Before
  public void setUp() {
    PortalContainer container = PortalContainer.getInstance();
    dlpOperationDAO = container.getComponentInstanceOfType(DlpOperationDAO.class);
    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(container);
  }

  @After
  public void tearDown() {
    dlpOperationDAO.deleteAll();
    RequestLifeCycle.end();
  }

  @Test
  public void testDlpQueueCreation() throws IllegalAccessException, NoSuchFieldException {

    // Given
    List<DlpOperation> dlpOperations = dlpOperationDAO.findAll();
    assertEquals(0, dlpOperations.size());

    // When
    createDlpOperations();

    // Then
    assertEquals(3, dlpOperationDAO.findAll().size());
    dlpOperations = dlpOperationDAO.findAll();
    Field privateField = DlpOperation.class.getDeclaredField("timestamp");
    privateField.setAccessible(true);
    Date timestamp = (Date) privateField.get(dlpOperations.get(0));
    assertNull(timestamp);
  }

  @Test
  public void testFindByEntityIdAndType() {

    // Given
    assertEquals(0, dlpOperationDAO.findAll().size());
    assertEquals(0, dlpOperationDAO.findByEntityIdAndType("1", "file").size());
    assertEquals(0, dlpOperationDAO.findByEntityIdAndType("100", "file").size());

    // When
    createDlpOperations();

    // Then
    assertEquals(3, dlpOperationDAO.findAll().size());
    assertEquals(1, dlpOperationDAO.findByEntityIdAndType("1", "file").size());
    assertEquals(0, dlpOperationDAO.findByEntityIdAndType("100", "file").size());
  }

  @Test
  public void testFindAllFirst() {

    // Given
    assertEquals(0, dlpOperationDAO.findAll().size());
    assertEquals(0, dlpOperationDAO.findAllFirstWithOffset(0, 2).size());

    // When
    createDlpOperations();

    // Then
    assertEquals(3, dlpOperationDAO.findAll().size());
    assertEquals(2, dlpOperationDAO.findAllFirstWithOffset(0, 2).size());
    assertEquals("22", dlpOperationDAO.findAllFirstWithOffset(0, 2).get(1).getEntityId());
    assertEquals("22", dlpOperationDAO.findAllFirstWithOffset(1, 2).get(0).getEntityId());
    assertEquals("100", dlpOperationDAO.findAllFirstWithOffset(1, 2).get(1).getEntityId());

  }

  @Test
  public void testDeleteByEntityId() {

    // Given
    assertEquals(0, dlpOperationDAO.findAll().size());
    assertEquals(0, dlpOperationDAO.findAllFirstWithOffset(0, 2).size());

    // When
    createDlpOperations();
    createDlpOperations();

    // Then
    assertEquals(6, dlpOperationDAO.findAll().size());
    assertEquals(2, dlpOperationDAO.findByEntityIdAndType("22", "file").size());
    dlpOperationDAO.deleteByEntityId("22");
    assertEquals(0, dlpOperationDAO.findByEntityIdAndType("22", "file").size());
    assertEquals(4, dlpOperationDAO.findAll().size());

  }

  private void createDlpOperations() {
    DlpOperation dlpOperation = new DlpOperation();
    dlpOperation.setEntityType("file");
    dlpOperation.setEntityId("1");
    dlpOperationDAO.create(dlpOperation);
    dlpOperation = new DlpOperation();
    dlpOperation.setEntityType("file");
    dlpOperation.setEntityId("22");
    dlpOperationDAO.create(dlpOperation);
    dlpOperation = new DlpOperation();
    dlpOperation.setEntityType("activity");
    dlpOperation.setEntityId("100");
    dlpOperationDAO.create(dlpOperation);
  }

}
