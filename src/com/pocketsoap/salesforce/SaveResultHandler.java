// Copyright (c) 2011 Simon Fell
//
// Permission is hereby granted, free of charge, to any person obtaining a 
// copy of this software and associated documentation files (the "Software"), 
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense, 
// and/or sell copies of the Software, and to permit persons to whom the 
// Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included 
// in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
// OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN 
// THE SOFTWARE.
//

package com.pocketsoap.salesforce;

import java.util.*;

import org.xml.sax.*;

/** parses one or more SaveResults from a soap response */
public class SaveResultHandler extends SoapFaultHandler {

	private List<SaveResult> results = new ArrayList<SaveResult>();
	private SaveResult sr;

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (localName.equals("result"))
			sr = new SaveResult();
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		if (sr == null) return;
		if (localName.equals("id"))
			sr.id = chars.toString();
		else if (localName.equals("success")) {
			String v = chars.toString();
			sr.success = v.equals("true") || v.equals("1");
		} else if (localName.equals("message") || localName.equals("statusCode")) {
			Error e;
			if (sr.errors == null) {
				e = new Error();
				sr.errors = new ArrayList<Error>(1);
				sr.errors.add(e);
			} else {
				e = sr.errors.get(0);
			}
			if (localName.equals("message"))
				e.message = chars.toString();
			else
				e.errorCode = chars.toString();
		} else if (localName.equals("result")) {
			results.add(sr);
			sr = null;
		}
	}

	public List<SaveResult> getResults() {
		return results;
	}	
}
