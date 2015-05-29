/*
 * Copyright 2006-2015 The MZmine 2 Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine 2; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.main;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.net.HttpURLConnection;

public class GoogleAnalyticsTracker implements Runnable {

    // Parameters
    String trackingUrl = "http://www.google-analytics.com/__utm.gif";
    String trackingCode = "UA-63013892-1"; // Google Analytics Tracking Code
    String hostName = "localhost"; // Host name
    String userAgent = null; // User Agent name
    String os = "Unknown"; // Operating System
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); // Screen Size
    String systemLocale = Locale.getDefault().toString().replace("_", "-"); // Language
    String pageTitle, pageUrl;

    Random random = new Random();

    public GoogleAnalyticsTracker(String title, String url) {
	pageTitle = title;
	pageUrl = url;
    }

    public void run() {
	// Only send data if SendStatistics variable is not set to 0
	String SendStatistics = System.getenv().get("MZ_STATISTICS");
	if (SendStatistics == null) {
	    SendStatistics = "1";
	}
	if (!SendStatistics.equals("0")) {
	    if (hostName.equals("localhost")) {
		try {
		    hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
		    // Ignore
		}
	    }

	    if (userAgent == null) {
		userAgent = "Java/" + System.getProperty("java.version");
		os = System.getProperty("os.arch");
		if (os == null || os.length() < 1) {
		    os = "";
		} else {
		    os += "; ";
		    os += System.getProperty("os.name") + " "
		       + System.getProperty("os.version");
		}
	    }

	    String documentTitle = "Java: MZmine "
		    + MZmineCore.getMZmineVersion() + " - " + pageTitle;
	    int cookie = random.nextInt();
	    int randomValue = random.nextInt(2147483647) - 1;
	    long now = new Date().getTime();

	    StringBuffer url = new StringBuffer(trackingUrl);
	    url.append("?utmwv=1"); // Analytics version
	    url.append("&utmn=" + random.nextInt()); // Random int
	    url.append("&utmcs=UTF-8"); // Encoding
	    url.append("&utmsr=" + screenSize); // Screen size
	    url.append("&utmul=" + systemLocale); // User language
	    url.append("&utmje=1"); // Java Enabled
	    url.append("&utmcr=1"); // Carriage return
	    url.append("&utmdt=" + documentTitle.replace(" ", "%20")); // Document title
	    url.append("&utmhn=" + hostName);// Hostname
	    url.append("&utmp=" + pageUrl);// Document url
	    url.append("&utmac=" + trackingCode);// Google Analytics account
	    url.append("&utmcc=__utma%3D'"
		    + cookie
		    + "."
		    + randomValue
		    + "."
		    + now
		    + "."
		    + now
		    + "."
		    + now
		    + ".2%3B%2B__utmb%3D"
		    + cookie
		    + "%3B%2B__utmc%3D"
		    + cookie
		    + "%3B%2B__utmz%3D"
		    + cookie
		    + "."
		    + now
		    + ".2.2.utmccn%3D(direct)%7Cutmcsr%3D(direct)%7Cutmcmd%3D(none)%3B%2B__utmv%3D"
		    + cookie);

	    try {
		URL urlLink = new URL(url.toString());
		HttpURLConnection UC = (HttpURLConnection) urlLink
			.openConnection();
		UC.setInstanceFollowRedirects(true);
		UC.setRequestMethod("GET");
		UC.setRequestProperty("User-agent", userAgent + " (" + os + ")");
		UC.connect();
		
		int responseCode = UC.getResponseCode();
		if (responseCode != HttpURLConnection.HTTP_OK) {
		    // Ignore
		} else {
		    // Ignore
		}
		
	    } catch (Exception e) {
		// Ignore
	    }

	}

    }

}