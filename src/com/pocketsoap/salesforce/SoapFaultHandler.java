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

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

/** parses a soap response looking to see if its a soap fault */
class SoapFaultHandler extends DefaultHandler {
	
	protected final StringBuilder chars = new StringBuilder();

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		chars.setLength(0);
		if (!seenSoapFault && localName.equals("Fault") && uri.equals(SoapProducer.SOAP_NS))
			seenSoapFault = true;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		chars.append(ch, start, length);
	}

	protected boolean seenSoapFault;
	protected String faultCode, faultString;
	
	boolean hasSeenSoapFault() {
		return seenSoapFault;
	}
	
	String getFaultCode() {
		return faultCode;
	}
	
	String getFaultString() {
		return faultString;
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (seenSoapFault) {
			if (localName.equals("faultcode"))
				faultCode = chars.toString();
			else if (localName.equals("faultstring"))
				faultString = chars.toString();
		}
	}
}