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

package com.pocketsoap.admin;

import android.os.AsyncTask;

/** base class for doing background API calls, and updating the UI with results, standardized error handling */
abstract class ApiAsyncTask<ParamType, ResultType> extends AsyncTask<ParamType, Void, ResultType> {

	/** the hosting activity should implement this, so that we can keep it upto date with whats happening */
	interface ActivityCallbacks {
		void setBusy(boolean b);
		void showError(Exception e);
	}
	
	ApiAsyncTask(ActivityCallbacks activity) {
		this.activity = activity;
	}

	protected final ActivityCallbacks activity;
	private Exception exception;
	
	@Override
	protected void onPreExecute() {
		activity.setBusy(true);
	}
	
	@Override
	protected final ResultType doInBackground(ParamType... params) {
		try {
			return doApiCall(params);
		} catch (Exception ex) {
			exception = ex;
		}
		return null;
	}

	protected abstract ResultType doApiCall(ParamType ... params) throws Exception;
	protected abstract void handleResult(ResultType result);
	
	@Override
	protected void onPostExecute(ResultType result) {
		activity.setBusy(false);
		if (exception == null) {
			handleResult(result);
		} else {
			handleError(exception);
		}
	}
	
	protected void handleError(Exception exception) {
		activity.showError(exception);
	}
}