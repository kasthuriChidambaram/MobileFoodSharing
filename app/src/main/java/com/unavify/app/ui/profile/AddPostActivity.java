package com.unavify.app.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.unavify.app.R;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class AddPostActivity extends AppCompatActivity {
    private static final String TAG = "AddPostActivity";
    private static final int MAX_IMAGE_SIZE_MB = 10;
    private static final int MAX_IMAGE_DIMENSION = 1080;
    private static final int MAX_VIDEO_SIZE_MB = 50;
    private static final int MIN_VIDEO_DURATION_SEC = 15;
    private static final int MAX_VIDEO_DURATION_SEC = 60;
    private static final int MAX_VIDEO_RESOLUTION = 720;
    private static final int COMPRESSION_QUALITY = 85;

    private ImageView mediaPreview;
    private EditText captionEditText;
    private Button uploadButton;
    private ProgressBar progressBar;
    private Uri selectedMediaUri;
    private boolean isVideo = false;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        executorService = Executors.newFixedThreadPool(2);
        
        mediaPreview = findViewById(R.id.media_preview);
        captionEditText = findViewById(R.id.caption_edit_text);
        uploadButton = findViewById(R.id.upload_button);
        progressBar = findViewById(R.id.progress_bar);

        showMediaPickerDialog();

        uploadButton.setOnClickListener(v -> {
            if (selectedMediaUri == null) {
                return;
            }
            String caption = captionEditText.getText().toString().trim();
            if (TextUtils.isEmpty(caption)) {
                return;
            }
            validateAndUploadMedia(selectedMediaUri, caption);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    private void showMediaPickerDialog() {
        String[] options = {"Take Photo", "Select Photo from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Capture or Select Photo")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Take Photo
                            pendingAction = 0; // Set pending action for camera
                            if (checkCameraPermission()) {
                                takePhotoLauncher.launch(null);
                                pendingAction = -1; // Reset if permission already granted
                            }
                            break;
                        case 1: // Select Photo
                            pendingAction = 1; // Set pending action for gallery
                            if (checkStoragePermission()) {
                                pickPhotoLauncher.launch("image/*");
                                pendingAction = -1; // Reset if permission already granted
                            }
                            break;
                    }
                })
                .setCancelable(true)
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // User cancelled, go back to previous screen
                    finish();
                })
                .setOnCancelListener(dialog -> {
                    // User cancelled by tapping outside, go back to previous screen
                    finish();
                })
                .show();
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
            return false;
        }
        return true;
    }

    private boolean checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses granular permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                    != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO}, 
                    STORAGE_PERMISSION_REQUEST);
                return false;
            }
        } else {
            // Android 12 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST);
                return false;
            }
        }
        return true;
    }

    private static final int CAMERA_PERMISSION_REQUEST = 1001;
    private static final int STORAGE_PERMISSION_REQUEST = 1002;
    
    // Track which action was requested before permission check
    private int pendingAction = -1; // -1 = none, 0 = camera, 1 = gallery

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, directly launch camera
                if (pendingAction == 0) {
                    takePhotoLauncher.launch(null);
                    pendingAction = -1; // Reset pending action
                }
            }
        } else if (requestCode == STORAGE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, directly launch gallery picker
                if (pendingAction == 1) {
                    pickPhotoLauncher.launch("image/*");
                    pendingAction = -1; // Reset pending action
                }
            }
        }
    }

    private final ActivityResultLauncher<Void> takePhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            bitmap -> {
                if (bitmap != null) {
                    selectedMediaUri = getImageUri(bitmap);
                    isVideo = false;
                    mediaPreview.setImageBitmap(bitmap);
                }
            }
    );

    private final ActivityResultLauncher<String> pickPhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedMediaUri = uri;
                    isVideo = false;
                    mediaPreview.setImageURI(uri);
                }
            }
    );

    private final ActivityResultLauncher<Uri> takeVideoLauncher = registerForActivityResult(
            new ActivityResultContracts.CaptureVideo(),
            success -> {
                if (success && selectedMediaUri != null) {
                    isVideo = true;
                    mediaPreview.setImageURI(selectedMediaUri);
                }
            }
    );

    private final ActivityResultLauncher<String> pickVideoLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedMediaUri = uri;
                    isVideo = true;
                    mediaPreview.setImageURI(uri);
                }
            }
    );

    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "IMG_" + System.currentTimeMillis(), null);
        return Uri.parse(path);
    }

    private Uri createVideoUri() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String videoFileName = "VID_" + timeStamp + ".mp4";
            File videoFile = new File(getExternalFilesDir(null), videoFileName);
            return FileProvider.getUriForFile(this, 
                getApplicationContext().getPackageName() + ".fileprovider", videoFile);
        } catch (Exception e) {
            Log.e(TAG, "Error creating video URI", e);
            return null;
        }
    }

    private void validateAndUploadMedia(Uri mediaUri, String caption) {
        progressBar.setVisibility(View.VISIBLE);
        uploadButton.setEnabled(false);
        progressBar.setProgress(0);



        executorService.execute(() -> {
            try {
                // Validate media
                ValidationResult validation = validateMedia(mediaUri);
                if (!validation.isValid) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        uploadButton.setEnabled(true);
                        Toast.makeText(this, validation.errorMessage, Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                // Check if compression will be needed
                if (!isVideo) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    InputStream inputStream = getContentResolver().openInputStream(mediaUri);
                    BitmapFactory.decodeStream(inputStream, null, options);
                    inputStream.close();
                    
                    int maxDimension = Math.max(options.outWidth, options.outHeight);
                }

                // Compress media if needed
                Uri processedUri = processMedia(mediaUri);
                if (processedUri == null) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        uploadButton.setEnabled(true);
                    });
                    return;
                }

                // Upload to Firebase
                runOnUiThread(() -> {
                    uploadToFirebase(processedUri, caption, isVideo ? "videos" : "images");
                });

            } catch (Exception e) {
                Log.e(TAG, "Error processing media", e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    uploadButton.setEnabled(true);
                });
            }
        });
    }

    private ValidationResult validateMedia(Uri mediaUri) {
        try {
            // Check file size first (before any processing)
            long fileSize = getFileSize(mediaUri);
            long maxSize = isVideo ? MAX_VIDEO_SIZE_MB * 1024 * 1024L : MAX_IMAGE_SIZE_MB * 1024 * 1024L;
            
            if (fileSize > maxSize) {
                String sizeLimit = isVideo ? MAX_VIDEO_SIZE_MB + "MB" : MAX_IMAGE_SIZE_MB + "MB";
                return new ValidationResult(false, "File size exceeds limit of " + sizeLimit);
            }

            // File size is acceptable, now check other validations
            if (isVideo) {
                return validateVideo(mediaUri);
            } else {
                return validateImage(mediaUri);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error validating media", e);
            return new ValidationResult(false, "Failed to validate media file");
        }
    }

    private ValidationResult validateVideo(Uri videoUri) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(this, videoUri);
            
            // Check duration
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (durationStr != null) {
                long durationMs = Long.parseLong(durationStr);
                long durationSec = durationMs / 1000;
                
                if (durationSec < MIN_VIDEO_DURATION_SEC) {
                    return new ValidationResult(false, "Video must be at least " + MIN_VIDEO_DURATION_SEC + " seconds long");
                }
                if (durationSec > MAX_VIDEO_DURATION_SEC) {
                    return new ValidationResult(false, "Video must be no longer than " + MAX_VIDEO_DURATION_SEC + " seconds");
                }
            }

            // Check resolution
            String widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            
            if (widthStr != null && heightStr != null) {
                int width = Integer.parseInt(widthStr);
                int height = Integer.parseInt(heightStr);
                int maxDimension = Math.max(width, height);
                
                if (maxDimension > MAX_VIDEO_RESOLUTION) {
                    return new ValidationResult(false, "Video resolution must be " + MAX_VIDEO_RESOLUTION + "p or lower");
                }
            }

            retriever.release();
            return new ValidationResult(true, null);

        } catch (Exception e) {
            Log.e(TAG, "Error validating video", e);
            return new ValidationResult(false, "Failed to validate video file");
        }
    }

    private ValidationResult validateImage(Uri imageUri) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            int width = options.outWidth;
            int height = options.outHeight;
            int maxDimension = Math.max(width, height);

            // Always accept images - compression will handle high resolution
            if (maxDimension > MAX_IMAGE_DIMENSION) {
                Log.i(TAG, "High resolution image detected: " + width + "x" + height + 
                      " (max: " + maxDimension + "). Will be automatically compressed to " + MAX_IMAGE_DIMENSION + "p");
            } else {
                Log.i(TAG, "Image resolution is within limits: " + width + "x" + height + 
                      " (max: " + maxDimension + "). No compression needed.");
            }

            return new ValidationResult(true, null);

        } catch (Exception e) {
            Log.e(TAG, "Error validating image", e);
            return new ValidationResult(false, "Failed to validate image file");
        }
    }

    private Uri processMedia(Uri mediaUri) {
        try {
            if (isVideo) {
                // Basic video validation - in production, implement compression
                // For now, we'll use the original but validate it
                long fileSize = getFileSize(mediaUri);
                if (fileSize > MAX_VIDEO_SIZE_MB * 1024 * 1024L) {
                    Log.w(TAG, "Video file is large: " + (fileSize / (1024 * 1024)) + "MB");
                    // TODO: Implement video compression here
                    // For production, use MediaCodec or FFmpeg to compress video
                }
                return mediaUri;
            } else {
                // Compress image
                return compressImage(mediaUri);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing media", e);
            return null;
        }
    }

    private Uri compressImage(Uri imageUri) {
        try {
            // First, get image dimensions without loading the full bitmap
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            int originalWidth = options.outWidth;
            int originalHeight = options.outHeight;
            int maxDimension = Math.max(originalWidth, originalHeight);
            
            // Calculate scale factor for efficient loading
            int scaleFactor = 1;
            if (maxDimension > MAX_IMAGE_DIMENSION) {
                scaleFactor = (int) Math.ceil((double) maxDimension / MAX_IMAGE_DIMENSION);
                Log.i(TAG, "Compressing image from " + originalWidth + "x" + originalHeight + 
                      " to fit within " + MAX_IMAGE_DIMENSION + "p limit (scale factor: " + scaleFactor + ")");
            }

            // Load bitmap with appropriate scale factor to save memory
            options.inJustDecodeBounds = false;
            options.inSampleSize = scaleFactor;
            options.inPreferredConfig = Bitmap.Config.RGB_565; // Use less memory
            
            inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
            
            if (originalBitmap == null) return null;

            // If still needs resizing, do it
            int currentWidth = originalBitmap.getWidth();
            int currentHeight = originalBitmap.getHeight();
            int currentMaxDimension = Math.max(currentWidth, currentHeight);
            
            if (currentMaxDimension <= MAX_IMAGE_DIMENSION) {
                // No need to resize, just compress
                return compressBitmap(originalBitmap);
            }

            // Final resize to exact dimensions
            float scale = (float) MAX_IMAGE_DIMENSION / currentMaxDimension;
            int newWidth = Math.round(currentWidth * scale);
            int newHeight = Math.round(currentHeight * scale);

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
            originalBitmap.recycle();

            Log.i(TAG, "Image compressed to: " + newWidth + "x" + newHeight);
            return compressBitmap(resizedBitmap);

        } catch (Exception e) {
            Log.e(TAG, "Error compressing image", e);
            return null;
        }
    }

    private Uri compressBitmap(Bitmap bitmap) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String fileName = "IMG_" + timeStamp + ".jpg";
            File compressedFile = new File(getExternalFilesDir(null), fileName);

            FileOutputStream fos = new FileOutputStream(compressedFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, fos);
            fos.close();
            bitmap.recycle();

            return FileProvider.getUriForFile(this, 
                getApplicationContext().getPackageName() + ".fileprovider", compressedFile);

        } catch (Exception e) {
            Log.e(TAG, "Error compressing bitmap", e);
            return null;
        }
    }

    private long getFileSize(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return 0;
            
            long size = 0;
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                size += bytesRead;
            }
            inputStream.close();
            return size;
        } catch (Exception e) {
            Log.e(TAG, "Error getting file size", e);
            return 0;
        }
    }

    private void uploadToFirebase(Uri mediaUri, String caption, String folder) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String fileName = folder + "/" + userId + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(fileName);
        UploadTask uploadTask = storageRef.putFile(mediaUri);

        uploadTask.addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            progressBar.setProgress((int) progress);
        }).addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
            savePostToFirestore(downloadUri.toString(), caption, folder.equals("videos"));
        })).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            uploadButton.setEnabled(true);
        });
    }

    private void savePostToFirestore(String mediaUrl, String caption, boolean isVideo) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String phone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        boolean isPublic = true; // Always set to true for now
        Post post = new Post(userId, phone, mediaUrl, caption, isVideo, isPublic, System.currentTimeMillis());
        FirebaseFirestore.getInstance().collection("posts")
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    
                    // Set result to indicate successful post upload
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("post_uploaded", true);
                    setResult(RESULT_OK, resultIntent);
                    Log.d("AddPostActivity", "Setting result: post_uploaded=true, RESULT_OK");
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    uploadButton.setEnabled(true);
                });
    }

    // Validation result class
    private static class ValidationResult {
        final boolean isValid;
        final String errorMessage;

        ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }
    }

    // Simple Post model for Firestore
    public static class Post {
        public String userId;
        public String phone;
        public String mediaUrl;
        public String caption;
        public boolean isVideo;
        public boolean isPublic;
        public long timestamp;
        public Post() {}
        public Post(String userId, String phone, String mediaUrl, String caption, boolean isVideo, boolean isPublic, long timestamp) {
            this.userId = userId;
            this.phone = phone;
            this.mediaUrl = mediaUrl;
            this.caption = caption;
            this.isVideo = isVideo;
            this.isPublic = isPublic;
            this.timestamp = timestamp;
        }
    }
} 