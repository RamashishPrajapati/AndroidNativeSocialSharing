package com.ram.projectlib.interfaces;

import java.util.ArrayList;

/**
 * Created by Ramashish Prajapati on 27-02-2019
 **/
public interface PermissionResultCallback {
    void PermissionGranted(int request_code);

    void PartialPermissionGranted(int request_code, ArrayList<String> granted_permissions);

    void PermissionDenied(int request_code);

    void NeverAskAgain(int request_code);
}
