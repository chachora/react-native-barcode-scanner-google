// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.ekreutz.barcodescanner.util;

import android.support.annotation.NonNull;
import android.util.Log;

import com.ekreutz.barcodescanner.ui.BarcodeScannerView;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.io.IOException;
import java.util.List;

/** Barcode Detector Demo. */
public class BarcodeScanningProcessor extends VisionProcessorBase<List<FirebaseVisionBarcode>> {

  private static final String TAG = "BarcodeScanProc";
  private static final String BARCODE_FOUND_KEY = "barcode_found";
  private final FirebaseVisionBarcodeDetector detector;
  private final BarcodeScannerView mView;

  public BarcodeScanningProcessor(BarcodeScannerView view) {
    // Note that if you know which format of barcode your app is dealing with, detection will be
    // faster to specify the supported barcode formats one by one, e.g.
    // new FirebaseVisionBarcodeDetectorOptions.Builder()
    //     .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
    //     .build();
    mView = view;
    detector = FirebaseVision.getInstance().getVisionBarcodeDetector();
  }

  @Override
  public void stop() {
    try {
      detector.close();
    } catch (IOException e) {
      Log.e(TAG, "Exception thrown while trying to close Barcode Detector: " + e);
    }
  }

  @Override
  protected Task<List<FirebaseVisionBarcode>> detectInImage(FirebaseVisionImage image) {
    return detector.detectInImage(image);
  }

  @Override
  protected void onSuccess(@NonNull List<FirebaseVisionBarcode> barcodes) {

    for (FirebaseVisionBarcode barcode:
         barcodes) {
      // Act on new barcode found
      WritableMap event = Arguments.createMap();
      event.putString("data", barcode.getRawValue());
      event.putString("type", BarcodeFormat.get(barcode.getValueType()));

      mView.sendNativeEvent(BARCODE_FOUND_KEY, event);
    }
  }

  @Override
  public boolean isOperational() {
    // TODO find out how to get state of Firebase Vision
    return true;
  }

  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.e(TAG, "Barcode detection failed " + e);
  }
}
