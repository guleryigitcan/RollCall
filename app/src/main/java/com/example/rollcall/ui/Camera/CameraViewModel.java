//Okan Ari 200001696
//Yiğitcan Güler 200001709
package com.example.rollcall.ui.Camera;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CameraViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public CameraViewModel() {
        mText = new MutableLiveData<>();

    }

    public LiveData<String> getText() {
        return mText;
    }
}