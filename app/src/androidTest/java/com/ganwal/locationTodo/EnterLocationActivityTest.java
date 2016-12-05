package com.ganwal.locationTodo;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.AutoCompleteTextView;

import com.ganwal.locationTodo.ui.EnterLocationActivity;


public class EnterLocationActivityTest extends
        ActivityInstrumentationTestCase2<EnterLocationActivity> {

    private EnterLocationActivity mActivity;


    public EnterLocationActivityTest() {
        super(EnterLocationActivity.class);
    }



    public EnterLocationActivityTest(Class<EnterLocationActivity> enterLocationActivity) {
        super(enterLocationActivity);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();

    }

    public void testPreconditions() {

    }

    //make sure when user types start entering address the right address shows up in drop-down
    @SmallTest
    public void testTextViewPresent() {
        final AutoCompleteTextView textView = (AutoCompleteTextView) mActivity.findViewById(R.id.enter_location_auto);
        assertNotNull(textView);

    }

    @UiThreadTest
    public void testEnterTextView() {

        int noOfResults;
        final String searchStr = "rdu airport";
        //get the auto-complete text view
        final AutoCompleteTextView textView = (AutoCompleteTextView) mActivity.findViewById(R.id.enter_location_auto);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.requestFocus();
                textView.setText(searchStr);
            }
        });
        assertEquals(searchStr, textView.getText().toString());

    }
    //make sure when user types start entering address the right address shows up in drop-down
    @UiThreadTest
    public void testAreGooglePlacesSuggestions() {

        final int noOfResults;
        final String searchStr = "times square";
        //get the auto-complete text view
        final AutoCompleteTextView textView = (AutoCompleteTextView) mActivity.findViewById(R.id.enter_location_auto);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.requestFocus();
                textView.setText(searchStr);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                assertTrue(textView.getAdapter().getCount() > 1);
            }
        });
    }


    //make sure the needed address is shown
    @UiThreadTest
    public void testIsAddressShowing() {

        final int noOfResults;
        final String searchStr = "times square";
        //get the auto-complete text view
        final AutoCompleteTextView textView = (AutoCompleteTextView) mActivity.findViewById(R.id.enter_location_auto);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.requestFocus();
                textView.setText(searchStr);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int noOfItems = textView.getAdapter().getCount();
                for(int i=0;i<noOfItems;i++) {
                    LocationPlace place  = (LocationPlace)textView.getAdapter().getItem(i);
                    if(place.getDescr().matches(searchStr)){
                        assertTrue(place.getDescr().matches(searchStr));
                    }
                }
            }
        });
    }



}
