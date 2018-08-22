/*
 * Copyright 2006-2015 The MZmine 2 Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MZmine 2; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package net.sf.mzmine.modules.peaklistmethods.isotopes.isotopepeakscanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import net.sf.mzmine.datamodel.PeakList;
import net.sf.mzmine.datamodel.PeakListRow;

/**
 * This class can be used to handle peak lists by ID rather than row indices. Set up via the
 * constructor by passing a peak list or via setUp(PeakList) Rows can also be added manually, for
 * example if you want to create a result peak list that does not contain duplicates. Since this
 * class uses a tree map the results will be in order and duplicates will be overwritten, which is
 * why it has the method containsID to check.
 *
 */
public class PeakListHandler {

  private TreeMap<Integer, PeakListRow> map;

  public PeakListHandler() {
    map = new TreeMap<Integer, PeakListRow>();
  }

  public PeakListHandler(PeakList pL) {
    map = new TreeMap<Integer, PeakListRow>();
    setUp(pL);
  }

  /**
   * use this if you want to manage an existing PeakList
   * 
   * @param pL the peak list you want to manage
   */
  public void setUp(PeakList pL) {
    for (PeakListRow row : pL.getRows()) {
      map.put(row.getID(), row);
    }
  }

  /**
   * Manually add a PeakListRow
   * 
   * @param row row to be added
   */
  public void addRow(PeakListRow row) {
    map.put(row.getID(), row);
  }

  /**
   * 
   * @return number of rows handled with plh
   */
  public int size() {
    return map.size();
  }

  /**
   * 
   * @param ID ID to check for
   * @return true if contained, false if not
   */
  public boolean containsID(int ID) {
    return map.containsKey(ID);
  }

  /**
   * 
   * @return ArrayList<Integer> of all IDs of the peak list rows
   */
  public ArrayList<Integer> getAllKeys() {
    Set<Integer> set = map.keySet();
    ArrayList<Integer> list = new ArrayList<Integer>(set);

    return list;
  }

  /**
   * 
   * @param ID ID of the row you want
   * @return Row with specified ID
   */
  public PeakListRow getRowByID(int ID) {
    return map.get(ID);
  }

  /**
   * 
   * @param ID integer array of IDs
   * @return all rows with specified ids
   */
  public PeakListRow[] getRowsByID(int ID[]) {
    PeakListRow[] rows = new PeakListRow[ID.length];

    for (int i = 0; i < ID.length; i++)
      rows[i] = map.get(ID[i]);

    return rows;
  }
}
