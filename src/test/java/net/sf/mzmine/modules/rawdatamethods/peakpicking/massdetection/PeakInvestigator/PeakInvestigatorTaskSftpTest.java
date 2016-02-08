package net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import net.sf.mzmine.datamodel.RawDataFile;
import net.sf.mzmine.datamodel.Scan;
import net.sf.mzmine.datamodel.impl.SimpleScan;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.PeakInvestigatorTask.ResponseErrorException;
import net.sf.mzmine.project.impl.RawDataFileImpl;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;

import com.veritomyx.PeakInvestigatorSaaS;
import com.veritomyx.actions.BaseAction;
import com.veritomyx.actions.BaseAction.ResponseFormatException;
import com.veritomyx.actions.SftpAction;

public class PeakInvestigatorTaskSftpTest {

	private ArgumentCaptor<SftpAction> actionCaptor = ArgumentCaptor
			.forClass(SftpAction.class);

	@Test
	public void testUploadFileToServerOk() throws IllegalStateException,
			ResponseFormatException, ResponseErrorException {

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
			ResponseFormatException, ResponseErrorException {

		PeakInvestigatorTask task = createDefaultTask(BaseAction.API_SOURCE);
		task.uploadFileToServer(new File("test.tar"));

		fail("Should not reach here.");
	}

	/**
	 * Test PeakInvestigatorTask.upoadFileToServer() with real ERROR response.
	 */
	@Test(expected = ResponseErrorException.class)
	public void testInitializeResponseError() throws IllegalStateException,
			ResponseFormatException, ResponseErrorException {

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
	private PeakInvestigatorTask createDefaultTask(String response) {
		PeakInvestigatorSaaS vtmx = mock(PeakInvestigatorSaaS.class);

		when(vtmx.putFile(actionCaptor.capture(), argThat(new IsFile())))
				.thenReturn(true);
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

	private class IsFile extends ArgumentMatcher<File> {

		@Override
		public boolean matches(Object argument) {
			if (argument instanceof File) {
				return true;
			}

			return false;
		}
	}

}
