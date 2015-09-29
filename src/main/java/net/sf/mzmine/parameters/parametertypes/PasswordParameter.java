/*
 * Copyright 20013-2014 The MZminePI Development Team
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
 * MZmine 2; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * St, Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.parameters.parametertypes;

import java.util.Arrays;
import java.util.Collection;

import javax.swing.JPasswordField;

import net.sf.mzmine.parameters.UserParameter;

import org.jasypt.util.text.BasicTextEncryptor;
import org.w3c.dom.Element;

public class PasswordParameter implements UserParameter<String, JPasswordField>
{
    private String name, description, value;
    private BasicTextEncryptor textEncryptor = null;

    public PasswordParameter(String name, String description)
    {
    	this(name, description, null);
    }

    public PasswordParameter(String name, String description, String defaultValue)
    {
		this.name        = name;
		this.description = description;
		this.value       = defaultValue;

		textEncryptor = new BasicTextEncryptor();
		// this key must remain the same for decryption to work.
		// could improve security by not having this embedded in the code, but...
		textEncryptor.setPassword("KweiourIU234kerj925lkjdf adfiL3899k");
	}

    /**
     * @see net.sf.mzmine.data.Parameter#getName()
     */
    @Override
    public String getName()
    {
    	return name;
    }

    /**
     * @see net.sf.mzmine.data.Parameter#getDescription()
     */
    @Override
    public String getDescription()
    {
    	return description;
    }

    @Override
    public JPasswordField createEditingComponent()
    {
    	return new JPasswordField(20);
    }

    public String getValue()
    {
    	return value;
    }

    @Override
    public void setValue(String value)
    {
    	this.value = value;
    }

    @Override
    public PasswordParameter cloneParameter()
    {
		PasswordParameter copy = new PasswordParameter(name, description);
		copy.setValue(this.getValue());
		return copy;
    }

    @Override
    public String toString()
    {
    	return name;
    }

    @Override
    public void setValueFromComponent(JPasswordField component)
    {
    	char[] pw = component.getPassword();
    	value = new String(pw);
    	Arrays.fill(pw, '0');	//Zero out the possible password, for security.
    }

    @Override
    public void setValueToComponent(JPasswordField component, String newValue)
    {
	    component.setText(value);
    }

    @Override
    public void loadValueFromXML(Element xmlElement)
    {
    	String data = xmlElement.getTextContent();
    	value = textEncryptor.decrypt(data);
    }

    @Override
    public void saveValueToXML(Element xmlElement)
    {
		if (value == null)
		    return;
    	String data = textEncryptor.encrypt(value);
    	xmlElement.setTextContent(data);
    }

    @Override
    public boolean checkValue(Collection<String> errorMessages)
    {
		if ((value == null) || (value.trim().length() == 0))
		{
		    errorMessages.add(name + " is not set properly");
		    return false;
		}
		return true;
    }
}