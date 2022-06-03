package org.exoplatform.dlp.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.dlp.domain.DlpPositiveItemEntity;

public class DlpPositiveItemDAOTest {

  private DlpPositiveItemDAO dlpPositiveItemDAO;

  @Before
  public void setUp() {
    PortalContainer container = PortalContainer.getInstance();
    dlpPositiveItemDAO = container.getComponentInstanceOfType(DlpPositiveItemDAO.class);
    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(container);
    dlpPositiveItemDAO.deleteAll();
  }

  @After
  public void tearDown() {
    dlpPositiveItemDAO.deleteAll();
    RequestLifeCycle.end();
  }

  @Test
  public void testDlpPositiveItemsCreation() {
    // Given
    List<DlpPositiveItemEntity> dlpPositiveItemEntities = dlpPositiveItemDAO.getDlpPositiveItems(0, 20);
    assertEquals(0, dlpPositiveItemEntities.size());

    // When
    DlpPositiveItemEntity dlpPositiveItemEntity = new DlpPositiveItemEntity();
    dlpPositiveItemEntity.setType("file");
    dlpPositiveItemEntity.setTitle("file");
    dlpPositiveItemDAO.create(dlpPositiveItemEntity);

    DlpPositiveItemEntity dlpPositiveItemEntity1 = new DlpPositiveItemEntity();
    dlpPositiveItemEntity.setType("file");
    dlpPositiveItemEntity.setTitle("file1");
    dlpPositiveItemDAO.create(dlpPositiveItemEntity1);

    DlpPositiveItemEntity dlpPositiveItemEntity2 = new DlpPositiveItemEntity();
    dlpPositiveItemEntity.setType("file");
    dlpPositiveItemEntity.setTitle("file2");
    dlpPositiveItemDAO.create(dlpPositiveItemEntity2);

    // Then
    dlpPositiveItemEntities = dlpPositiveItemDAO.getDlpPositiveItems(0, 20);
    assertEquals(3, dlpPositiveItemEntities.size());
  }

  @Test
  public void testFindDlpPositiveItemByReference() {
    // Given
    assertEquals(0, dlpPositiveItemDAO.findAll().size());
    assertNull(dlpPositiveItemDAO.findDlpPositiveItemByReference("ref1"));

    // When
    DlpPositiveItemEntity dlpPositiveItemEntity = new DlpPositiveItemEntity();
    dlpPositiveItemEntity.setType("file");
    dlpPositiveItemEntity.setTitle("file");
    dlpPositiveItemEntity.setReference("ref1");
    dlpPositiveItemDAO.create(dlpPositiveItemEntity);

    // Then
    assertNotNull(dlpPositiveItemDAO.findDlpPositiveItemByReference("ref1"));
  }

  @Test
  public void testDeleteDlpPositiveItemByReference() {
    // When
    DlpPositiveItemEntity dlpPositiveItemEntity = new DlpPositiveItemEntity();
    dlpPositiveItemEntity.setType("file");
    dlpPositiveItemEntity.setTitle("file");
    dlpPositiveItemEntity.setReference("ref1");
    dlpPositiveItemDAO.create(dlpPositiveItemEntity);

    // Then
    assertEquals(1, dlpPositiveItemDAO.count().intValue());

    // when
    dlpPositiveItemDAO.delete(dlpPositiveItemEntity);

    // Then
    assertEquals(0, dlpPositiveItemDAO.count().intValue());
  }

  @Test
  public void testDlpPositiveItemsCreationWithLongKeyword() {
    // Given
    List<DlpPositiveItemEntity> dlpPositiveItemEntities = dlpPositiveItemDAO.getDlpPositiveItems(0, 20);
    assertEquals(0, dlpPositiveItemEntities.size());

    // When
    DlpPositiveItemEntity dlpPositiveItemEntity = new DlpPositiveItemEntity();
    dlpPositiveItemEntity.setType("file");
    dlpPositiveItemEntity.setTitle("file");
    dlpPositiveItemEntity.setKeywords("keyword1, " + "keyword2, " + "keyword3, " + "keyword4, " + "keyword5, " + "keyword6");
    dlpPositiveItemDAO.create(dlpPositiveItemEntity);

    // Then
    dlpPositiveItemEntities = dlpPositiveItemDAO.getDlpPositiveItems(0, 20);
    assertEquals(1, dlpPositiveItemEntities.size());
  }
}
