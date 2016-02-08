package net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import net.sf.mzmine.datamodel.RawDataFile;
import net.sf.mzmine.datamodel.Scan;
import net.sf.mzmine.datamodel.impl.SimpleScan;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.PeakInvestigatorTask.ResponseErrorException;
import net.sf.mzmine.project.impl.RawDataFileImpl;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.veritomyx.PeakInvestigatorSaaS;
import com.veritomyx.actions.BaseAction.ResponseFormatException;
import com.veritomyx.actions.BaseAction;
import com.veritomyx.actions.PrepAction;

public class PeakInvestigatorTaskPrepTest {

	private ArgumentCaptor<PrepAction> actionCaptor = ArgumentCaptor
			.forClass(PrepAction.class);

	@Test
	public void testCheckPrepAnalysisAnalyzing() throws IllegalStateException,
			ResponseFormatException, ResponseErrorException {

		PeakInvestigatorTask task = createDefaultTask(PrepAction.EXAMPLE_RESPONSE_1);
		task.checkPrepAnalysis("test.tar");

		assertEquals(PrepAction.Status.Analyzing, actionCaptor.getValue()
				.getStatus());
	}

	@Test
	public void testCheckPrepAnalysisReady() throws IllegalStateException,
			ResponseFormatException, ResponseErrorException {

		PeakInvestigatorTask task = createDefaultTask(PrepAction.EXAMPLE_RESPONSE_2);
		task.checkPrepAnalysis("test.tar");

		assertEquals(PrepAction.Status.Ready, actionCaptor.getValue()
				.getStatus());
	}

	/**
	 * Test PeakInvestigatorTask.checkPrepAnalysis() with HTML response.
	 */
	@Test(expected = ResponseFormatException.class)
	public void testInitializeResponseHTML() throws IllegalStateException,
			ResponseFormatException, ResponseErrorException {

		PeakInvestigatorTask task = createDefaultTask(BaseAction.API_SOURCE);
		task.checkPrepAnalysis("test.tar");

		fail("Should not reach here.");
	}

	/**
	 * Test PeakInvestigatorTask.checkPrepAnalysis() with real ERROR response.
	 */
	@Test(expected = ResponseErrorException.class)
	public void testInitializeResponseError() throws IllegalStateException,
			ResponseFormatException, ResponseErrorException {

		String response = BaseAction.ERROR_CREDENTIALS
				.replace("ACTION", "PREP");

		PeakInvestigatorTask task = createDefaultTask(response);
		task.checkPrepAnalysis("test.tar");

		fail("Should not reach here.");
	}

	/**
	 * Convenience function to build PeakInvestigatorTask that has setup with
	 * PeakInvestigatorSaaS and RawDataFile mocks.
	 */
	private PeakInvestigatorTask createDefaultTask(String response) {
		PeakInvestigatorSaaS vtmx = mock(PeakInvestigatorSaaS.class);
		when(vtmx.executeAction(actionCaptor.capture())).thenReturn(response);

		RawDataFile rawFile = mock(RawDataFileImpl.class);
		Scan scan = mock(SimpleScan.class);

		when(rawFile.getScanNumbers()).thenReturn(new int[] { 0, 1 });
		when(rawFile.getScan(anyInt())).thenReturn(scan);
		when(scan.getNumberOfDataPoints()).thenReturn(12345);

		PeakInvestigatorTask task = new PeakInvestigatorTask("test.com",
				"user", "password", 0).withService(vtmx).withRawDataFile(
				rawFile);
		return task;
	}

}
