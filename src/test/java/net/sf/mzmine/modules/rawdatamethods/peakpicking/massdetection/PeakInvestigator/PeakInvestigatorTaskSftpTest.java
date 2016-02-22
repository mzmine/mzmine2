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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
	@Rule public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testUploadFileToServer_Ok() throws IllegalStateException,
			ResponseFormatException, ResponseErrorException,
			FileNotFoundException, SftpException, JSchException,
			InterruptedException {

		PeakInvestigatorTask task = createUploadTask(
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
	@Test
	public void testUploadFileToServer_ResponseHTML() throws IllegalStateException,
			ResponseFormatException, ResponseErrorException,
			FileNotFoundException, JSchException, SftpException,
			InterruptedException {

		thrown.expect(ResponseFormatException.class);
		thrown.expectMessage("Server response appears to be HTML/XML");

		PeakInvestigatorTask task = createUploadTask(BaseAction.API_SOURCE);
		task.uploadFileToServer(new File("test.tar"));

		fail("Should not reach here.");
	}

	/**
	 * Test PeakInvestigatorTask.upoadFileToServer() with real ERROR response.
	 */
	@Test
	public void testUploadFileToServer_ResponseError() throws IllegalStateException,
			ResponseFormatException, ResponseErrorException,
			FileNotFoundException, JSchException, SftpException,
			InterruptedException {

		thrown.expect(ResponseErrorException.class);
		thrown.expectMessage("Invalid username or password");

		String response = BaseAction.ERROR_CREDENTIALS
				.replace("ACTION", "SFTP");

		PeakInvestigatorTask task = createUploadTask(response);
		task.uploadFileToServer(new File("test.tar"));

		fail("Should not reach here.");
	}

	/**
	 * Convenience function to build PeakInvestigatorTask that has setup with
	 * RawDataFile mocks.
	 */
	private PeakInvestigatorTask createDefaultTask(String response)
			throws FileNotFoundException, JSchException, SftpException {

		Scan scan = mock(SimpleScan.class);
		when(scan.getNumberOfDataPoints()).thenReturn(12345);

		RawDataFile rawFile = mock(RawDataFileImpl.class);
		when(rawFile.getScanNumbers()).thenReturn(new int[] { 0, 1 });
		when(rawFile.getScan(anyInt())).thenReturn(scan);

		PeakInvestigatorTask task = new PeakInvestigatorTask("test.com",
				"user", "password", 0).withRawDataFile(rawFile);

		return task;
	}

	private PeakInvestigatorTask createUploadTask(String response)
			throws JSchException, SftpException, FileNotFoundException {

		PeakInvestigatorSaaS vtmx = mock(PeakInvestigatorSaaS.class);
		doNothing().when(vtmx).putFile(actionCaptor.capture(), anyString(),
				anyString(), argThat(new IsMonitor()));
		when(vtmx.executeAction(actionCaptor.capture())).thenReturn(response);

		return createDefaultTask(response).withService(vtmx);

	}

	private PeakInvestigatorTask createDownloadTask(String response)
			throws JSchException, SftpException, FileNotFoundException {

		PeakInvestigatorSaaS vtmx = mock(PeakInvestigatorSaaS.class);
		doNothing().when(vtmx).getFile(actionCaptor.capture(), anyString(),
				anyString(), argThat(new IsMonitor()));
		when(vtmx.executeAction(actionCaptor.capture())).thenReturn(response);

		return createDefaultTask(response).withService(vtmx);

	}

	@Test
	public void testDownloadFileFromServer_Ok() throws IllegalStateException,
			ResponseFormatException, ResponseErrorException,
			FileNotFoundException, SftpException, JSchException,
			InterruptedException {

		PeakInvestigatorTask task = createDownloadTask(
				SftpAction.EXAMPLE_RESPONSE_1);
		task.downloadFileFromServer(new File("test.tar"), new File("test_local.tar"));

		SftpAction action = actionCaptor.getValue();
		assertEquals(action.getHost(), "peakinvestigator.veritomyx.com");
		assertEquals(action.getSftpUsername(), "V504");
		assertEquals(action.getSftpPassword(), "cB34lxCH0anR952gu");
		assertEquals(action.getPort(), 22022);
		assertEquals(action.getDirectory(), "/files");
	}

	/**
	 * Test PeakInvestigatorTask.downloadFileFromServer() with HTML response.
	 */
	@Test
	public void testDownloadFileFromServer_ResponseHTML()
			throws IllegalStateException, ResponseFormatException,
			ResponseErrorException, FileNotFoundException, JSchException,
			SftpException, InterruptedException {

		thrown.expect(ResponseFormatException.class);
		thrown.expectMessage("Server response appears to be HTML/XML");

		PeakInvestigatorTask task = createDownloadTask(BaseAction.API_SOURCE);
		task.downloadFileFromServer(new File("test.tar"), new File(
				"test_local.tar"));

		fail("Should not reach here.");
	}

	/**
	 * Test PeakInvestigatorTask.downloadFileFromServer() with real ERROR response.
	 */
	@Test
	public void testDownloadFileFromServer_ResponseError() throws IllegalStateException,
			ResponseFormatException, ResponseErrorException,
			FileNotFoundException, JSchException, SftpException,
			InterruptedException {

		thrown.expect(ResponseErrorException.class);
		thrown.expectMessage("Invalid username or password");

		String response = BaseAction.ERROR_CREDENTIALS
				.replace("ACTION", "SFTP");

		PeakInvestigatorTask task = createDownloadTask(response);
		task.downloadFileFromServer(new File("test.tar"), new File(
				"test_local.tar"));

		fail("Should not reach here.");
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
