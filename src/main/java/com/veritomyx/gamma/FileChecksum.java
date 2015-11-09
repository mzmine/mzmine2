package com.veritomyx.gamma;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * FileChecksum Class used to create and verify Veritomyx file checksums.
 * 
 * FileChecksum.version contains the version string.
 * 
 * FileChecksum.prefix contains the checksum line prefix string that can be used to recognize the hash line.
 * 
 * To validate the hash in a file, create an instance of the class then call validate() method.
 * The file must contain a hash appended with this class to validate properly.
 * 
 * To compute and append a hash to a new output file, create an instance and then
 * either call hash_line() for each line of the file you are writing or
 * call hash_file() to hash the entire file you have written.
 * Finally, to add the hash to the file, call the append_txt() or append_xml() method.
 * 
 * @author Dan Schmidt
 */
public class FileChecksum
{
	public static final String version = "1.0.1";
	public  final String       prefix  = "# checksum:";
	private final String       seed    = "Veritomyx hash seed!";
	private MessageDigest md;
	private String        filename;
	private File          file;
	private List<String>  lines;
	private String        sum;		// computed hash
	private String        fsum;		// hash read from file

	/**
	 * @param filename
	 * @throws Exception
	 */
	public FileChecksum(String filename) throws Exception
	{
		this.filename = filename;
		file = new File(filename);
		reset();
	}

	/**
	 * @param file
	 * @throws Exception
	 */
	public FileChecksum(File file) throws Exception
	{
		this.file = file;
		filename = file.getPath();
		reset();
	}

	/**
	 * Reset the hash values to those of a new instance.
	 * 
	 * @throws Exception
	 */
	public void reset() throws Exception
	{
		md    = MessageDigest.getInstance("SHA-1");
		fsum  = "";
		sum   = "";
		hash_line(seed);		// start with special hash seed
		lines = new ArrayList<String>();
	}

	/**
	 * Add a new line to the cumulative checksum hash.
	 * 
	 * @param line
	 * @return String		// returns input unmodified line
	 * @throws Exception
	 */
	public String hash_line(String line)
	{
		// calculate the hash of the last sum + the new line unless it starts with the prefix
		if (!line.startsWith(prefix))
		{
			String s = sum + line.replaceAll("\r?\n", "");
			md.update(s.getBytes());
			sum = toHex(md.digest());
		}
		return line;
	}

	/**
	 * Calculate the hash for the entire file specified.
	 * Any cumulative computed hash (hash_line()) will be lost.
	 * 
	 * @throws Exception
	 */
	public void hash_file() throws Exception
	{
		reset();
		String line;
		BufferedReader fd = openReadFile(filename);
		if (fd != null)
		{
			while ((line = fd.readLine()) != null)
			{
				if (line.startsWith(prefix))
				{
					fsum = line.substring(prefix.length());	// extract the hash from the file
					lines.add(line);
				}
				else
					lines.add(hash_line(line));		// calculate the hash of the last sum + the new line
			}
			fd.close();
		}
	}
	
	/**
	 * Return list of strings from file
	 * 
	 * @return
	 */
	public List<String> getFileStrings()
	{
		return lines;
	}

	/**
	 * Return the checksum string
	 * 
	 * @return
	 */
	public String checksum_line()
	{
		return prefix + sum + "\n";
	}

	/**
	 * Append the computed hash to the text file as a single comment line at the end of the file.
	 * The data should all have been pre-written to the file.
	 * 
	 * @param validate
	 * @return
	 * @throws Exception
	 */
	public boolean append_txt(boolean validate) throws Exception
	{
		BufferedWriter fd = openWriteFile(filename, true);
		fd.write(checksum_line());
		fd.close();
		boolean ret = (validate) ? verify(true) : true;
		return ret;
	}

	/**
	 * Append the computed hash to the XML file.
	 * The data should all have been pre-written to the file except for the 
	 * terminating tag which will be added by this method.
	 * The checksum will be enclosed in <chksum_tag>hash</ckksum_tag>
	 * and the file will be terminated with </file_tag>
	 * 
	 * @param chksum_tag
	 * @param file_tag
	 * @param validate
	 * @return
	 * @throws Exception
	 */
	public boolean append_xml(String chksum_tag, String file_tag, boolean validate) throws Exception
	{
		String tag1 = "<"  + chksum_tag + ">";
		String tag2 = "</" + chksum_tag + ">";
		String tag3 = "</" + file_tag   + ">";
		hash_line(tag1);				// add the three extra tag lines to the file hash
		hash_line(tag2);
		hash_line(tag3);
		String line = tag1 + "\n" + prefix + sum + "\n" + tag2 + "\n" + tag3 + "\n";	// output the hash and the tags
		BufferedWriter fd = openWriteFile(filename, true);
		fd.write(line);
		fd.close();
		boolean ret = (validate) ? verify(true) : true;
		return ret;
	}

	/**
	 * Verify the hash within the file matches the computed hash.
	 * Any cumulative computed hash (hash_line()) will be lost.
	 * 
	 * @param verbose
	 * @return
	 * @throws Exception
	 */
	public boolean verify(boolean verbose) throws Exception
	{
		hash_file();
		boolean good = sum.equals(fsum);
		if (verbose)
			System.out.println((good ? "Valid" : "Invalid") + " checksum in file " + file.getName());
		return good;
	}

	/**
	 * Open the proper type of buffered file depending on the .gz suffix
	 * 
	 * @param path
	 * @param append
	 * @return
	 * @throws IOException
	 */
	private BufferedWriter openWriteFile(String path, boolean append) throws IOException
	{
		BufferedWriter fd;
		if (path.endsWith(".gz"))
			fd = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(path, append))));
		else
			fd = new BufferedWriter(new FileWriter(path, append));
		return fd;
	}

	/**
	 * Open the proper type of buffered file depending on the .gz suffix
	 * 
	 * @param path
	 * @param mode
	 * @return
	 * @throws IOException
	 */
	private BufferedReader openReadFile(String path) throws IOException
	{
		BufferedReader fd;
		if (filename.endsWith(".gz"))
			fd = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(path))));
		else
			fd = new BufferedReader(new FileReader(path));
		return fd;
	}

	/**
	 * Convert the byte array to hex format String.
	 * 
	 * @param buf
	 * @return
	 */
	private String toHex(byte[] buf)
	{
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < buf.length; i++)
			sb.append(Integer.toString((buf[i] & 0xff) + 0x100, 16).substring(1));
		return sb.toString();
	}
}
