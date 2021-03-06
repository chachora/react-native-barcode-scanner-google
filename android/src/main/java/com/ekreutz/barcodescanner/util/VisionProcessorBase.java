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

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract base class for ML Kit frame processors. Subclasses need to implement {@link
 * #onSuccess(T)} to define what they want to with the detection
 * results and {@link #detectInImage(FirebaseVisionImage)} to specify the detector object.
 *
 * @param <T> The type of the detected feature.
 */
public abstract class VisionProcessorBase<T> implements VisionImageProcessor {

    // Whether we should ignore process(). This is usually caused by feeding input data faster than
    // the model can handle.
    private final AtomicBoolean shouldThrottle = new AtomicBoolean(false);
    private Bitmap bitmap;

    public VisionProcessorBase() {
    }

    // Bitmap version
    @Override
    public void process(Bitmap bitmap) {
        if (shouldThrottle.get()) {
            return;
        }
        this.bitmap = bitmap;
        detectInVisionImage(FirebaseVisionImage.fromBitmap(bitmap));
    }

    private void detectInVisionImage(FirebaseVisionImage image) {
        detectInImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<T>() {
                            @Override
                            public void onSuccess(T results) {
                                shouldThrottle.set(false);
                                VisionProcessorBase.this.onSuccess(results);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                shouldThrottle.set(false);
                                VisionProcessorBase.this.onFailure(e);
                            }
                        });
        // Begin throttling until this frame of input has been processed, either in onSuccess or
        // onFailure.
        shouldThrottle.set(true);
    }

    @Override
    public void stop() {
    }

    protected abstract Task<T> detectInImage(FirebaseVisionImage image);

    protected abstract void onSuccess(@NonNull T results);

    protected abstract void onFailure(@NonNull Exception e);
}
