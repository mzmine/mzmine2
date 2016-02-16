package net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileNotFoundException;

import net.sf.mzmine.datamodel.RawDataFile;
import net.sf.mzmine.datamodel.Scan;
import net.sf.mzmine.datamodel.impl.SimpleScan;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.PeakInvestigatorTask.ResponseErrorException;
import net.sf.mzmine.project.impl.RawDataFileImpl;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.veritomyx.PeakInvestigatorSaaS;
import com.veritomyx.actions.BaseAction;
import com.veritomyx.actions.BaseAction.ResponseFormatException;
import com.veritomyx.actions.SftpAction;

public class PeakInvestigatorTaskSftpTest {

	private ArgumentCaptor<SftpAction> actionCaptor = ArgumentCaptor
			.forClass(SftpAction.class);

	@Test
	public void testUploadFileToServerOk() throws IllegalStateException,
			ResponseFormatException, ResponseErrorException,
			FileNotFoundException, SftpException, JSchException {

		PeakInvestigatorTask task = createDefaultTask(
				SftpAction.EXAMPLE_RESPONSE_1);
		task.uploadFileToServer(new File("test.tar"));

		SftpAction action = actionCaptor.getValue();
		assertEquals(action.getHost(), "peakinvestigator.veritomyx.com");
		assertEquals(action.getSftpUsername(), "V504");
		assertEquals(action.getSftpPassword(), "cB34lxCH0anR952gu");
		assertEquals(action.getPort(), 22022);
		assertEquals(action.getDirectory(), "/files");
	}

	/**
	 * Test PeakInvestigatorTask.uploadFileToServer() with HTML response.
	 */
	@Test(expected = ResponseFormatException.class)
	public void testInitializeResponseHTML() throws IllegalStateException,
			ResponseFormatException, ResponseErrorException,
			FileNotFoundException, JSchException, SftpException {

		PeakInvestigatorTask task = createDefaultTask(BaseAction.API_SOURCE);
		task.uploadFileToServer(new File("test.tar"));

		fail("Should not reach here.");
	}

	/**
	 * Test PeakInvestigatorTask.upoadFileToServer() with real ERROR response.
	 */
	@Test(expected = ResponseErrorException.class)
	public void testInitializeResponseError() throws IllegalStateException,
			ResponseFormatException, ResponseErrorException,
			FileNotFoundException, JSchException, SftpException {

		String response = BaseAction.ERROR_CREDENTIALS
				.replace("ACTION", "SFTP");

		PeakInvestigatorTask task = createDefaultTask(response);
		task.uploadFileToServer(new File("test.tar"));

		fail("Should not reach here.");
	}

	/**
	 * Convenience function to build PeakInvestigatorTask that has setup with
	 * PeakInvestigatorSaaS and RawDataFile mocks.
	 */
	private PeakInvestigatorTask createDefaultTask(String response)
			throws FileNotFoundException, JSchException, SftpException {

		// setup PI Task
		PeakInvestigatorSaaS vtmx = mock(PeakInvestigatorSaaS.class);
		doNothing().when(vtmx).putFile(actionCaptor.capture(), anyString(),
				anyString(), argThat(new IsMonitor()));
		when(vtmx.executeAction(actionCaptor.capture())).thenReturn(response);

		Scan scan = mock(SimpleScan.class);
		when(scan.getNumberOfDataPoints()).thenReturn(12345);

		RawDataFile rawFile = mock(RawDataFileImpl.class);
		when(rawFile.getScanNumbers()).thenReturn(new int[] { 0, 1 });
		when(rawFile.getScan(anyInt())).thenReturn(scan);

		PeakInvestigatorTask task = new PeakInvestigatorTask("test.com",
				"user", "password", 0).withService(vtmx).withRawDataFile(
				rawFile);

		return task;
	}

	private class IsMonitor extends ArgumentMatcher<SftpProgressMonitor> {

		@Override
		public boolean matches(Object argument) {
			if (argument instanceof SftpProgressMonitor) {
				return true;
			}

			return false;
		}
	}

}
