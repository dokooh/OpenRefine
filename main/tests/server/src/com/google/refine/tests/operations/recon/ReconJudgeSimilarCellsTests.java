package com.google.refine.tests.operations.recon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.refine.browsing.EngineConfig;
import com.google.refine.model.AbstractOperation;
import com.google.refine.model.Cell;
import com.google.refine.model.Column;
import com.google.refine.model.Project;
import com.google.refine.model.Recon;
import com.google.refine.model.recon.ReconConfig;
import com.google.refine.model.recon.StandardReconConfig;
import com.google.refine.operations.OperationRegistry;
import com.google.refine.operations.recon.ReconJudgeSimilarCellsOperation;
import com.google.refine.process.Process;
import com.google.refine.tests.RefineTest;
import com.google.refine.tests.util.TestUtils;

public class ReconJudgeSimilarCellsTests extends RefineTest {
    
    static final EngineConfig ENGINE_CONFIG = EngineConfig.reconstruct(new JSONObject("{\"mode\":\"row-based\"}}"));
    
    @Override
    @BeforeTest
    public void init() {
        logger = LoggerFactory.getLogger(this.getClass());
        OperationRegistry.registerOperation(getCoreModule(), "recon-judge-similar-cells", ReconJudgeSimilarCellsOperation.class);
    }
    
    @Test
    public void serializeReconJudgeSimilarCellsOperation() throws JSONException {
        String json = "{\"op\":\"core/recon-judge-similar-cells\","
                + "\"description\":\"Mark to create one single new item for all cells containing \\\"foo\\\" in column A\","
                + "\"engineConfig\":{\"mode\":\"row-based\",\"facets\":[]},"
                + "\"columnName\":\"A\","
                + "\"similarValue\":\"foo\","
                + "\"judgment\":\"new\","
                + "\"shareNewTopics\":true}";
        TestUtils.isSerializedTo(ReconJudgeSimilarCellsOperation.reconstruct(new JSONObject(json)), json);
    }
    
    @Test
    public void testMarkNewTopics() throws Exception {
        Project project = createCSVProject(
                "A,B\n"
              + "foo,bar\n"
              + "alpha,beta\n");
        
        Column column = project.columnModel.columns.get(0);
        ReconConfig config = new StandardReconConfig(
                "http://my.database/recon_service",
                "http://my.database/entity/",
                "http://my.database/schema/",
                null,
                null,
                true, Collections.emptyList());
        column.setReconConfig(config);

        AbstractOperation op = new ReconJudgeSimilarCellsOperation(
            ENGINE_CONFIG,
            "A",
            "foo",
            Recon.Judgment.New,
            null, true);
        Process process = op.createProcess(project, new Properties());
        process.performImmediate();
        
        Cell cell = project.rows.get(0).cells.get(0);
        assertEquals(Recon.Judgment.New, cell.recon.judgment);
        assertEquals("http://my.database/entity/", cell.recon.identifierSpace);
        assertNull(project.rows.get(1).cells.get(0).recon);
    }
}
