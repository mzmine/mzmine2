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

import com.jcraft.jsch.JSchException;
import com.veritomyx.PeakInvestigatorSaaS;
import com.veritomyx.actions.BaseAction;
import com.veritomyx.actions.RunAction;
import com.veritomyx.actions.BaseAction.ResponseFormatException;

public class PeakInvestigatorTaskRunTest {

	private ArgumentCaptor<RunAction> actionCaptor = ArgumentCaptor
			.forClass(RunAction.class);

	@Test
	public void testInitiateRunOk() throws IllegalStateException,
			ResponseFormatException, ResponseErrorException, JSchException {

		PeakInvestigatorTask task = createDefaultTask(RunAction.EXAMPLE_RESPONSE_1);
		task.initiateRun("test.tar", "RTO-24");

		RunAction action = actionCaptor.getValue();
		assertEquals("P-504.1463", action.getJob());
	}

	/**
	 * Test PeakInvestigatorTask.initiateRun() with HTML response.
	 */
	@Test(expected = ResponseFormatException.class)
	public void testInitializeResponseHTML() throws IllegalStateException,
			ResponseFormatException, ResponseErrorException, JSchException {

		PeakInvestigatorTask task = createDefaultTask(BaseAction.API_SOURCE);
		task.initiateRun("test.tar", "RTO-24");

		fail("Should not reach here.");
	}

	/**
	 * Test PeakInvestigatorTask.initiateRun() with real ERROR response.
	 */
	@Test(expected = ResponseErrorException.class)
	public void testInitializeResponseError() throws IllegalStateException,
			ResponseFormatException, ResponseErrorException, JSchException {

		String response = BaseAction.ERROR_CREDENTIALS
				.replace("ACTION", "RUN");

		PeakInvestigatorTask task = createDefaultTask(response);
		task.initiateRun("test.tar", "RTO-24");

		fail("Should not reach here.");
	}

	/**
	 * Convenience function to build PeakInvestigatorTask that has setup with
	 * PeakInvestigatorSaaS and RawDataFile mocks.
	 */
	private PeakInvestigatorTask createDefaultTask(String response) throws JSchException {
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
