/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.fsu.cs.contextprovider.dialog;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import edu.fsu.cs.contextprovider.R;
import edu.fsu.cs.contextprovider.R.layout;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

/**
 * <h3>Dialog Activity</h3>
 * 
 * <p>This demonstrates the how to write an activity that looks like 
 * a pop-up dialog.</p>
 */
public class DialogActivity extends Activity {
    /**
     * Initialization of the Activity after it is first created.  Must at least
     * call {@link android.app.Activity#setContentView setContentView()} to
     * describe what is to be displayed in the screen.
     */
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        
        // See assets/res/any/layout/dialog_activity.xml for this
        // view layout definition, which is being set here as
        // the content of our screen.
        setContentView(R.layout.dialog_activity);
        
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, 
                android.R.drawable.ic_dialog_alert);
    }
}
