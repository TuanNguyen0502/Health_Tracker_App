package hcmute.edu.vn.healthtrackerapp.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import hcmute.edu.vn.healthtrackerapp.MedicalRecordsActivity;
import hcmute.edu.vn.healthtrackerapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MedicalRecordsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MedicalRecordsFragment extends Fragment {
    private static final String TAG = "MedicalRecordsFragment";
    private Button buttonUpload, buttonViewMedicalRecord;
    private EditText editTextImageTitle;
    private ImageView imageViewRecord;
    private Uri imagePath;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MedicalRecordsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MedicalRecordsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MedicalRecordsFragment newInstance(String param1, String param2) {
        MedicalRecordsFragment fragment = new MedicalRecordsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medical_records, container, false);

        buttonUpload = view.findViewById(R.id.buttonUpload);
        buttonViewMedicalRecord = view.findViewById(R.id.buttonViewMedicalRecord);
        imageViewRecord = view.findViewById(R.id.imageViewRecord);
        editTextImageTitle = view.findViewById(R.id.editTextImageTitle);

        initConfig();

        imageViewRecord.setOnClickListener(v -> selectImage());


        buttonUpload.setOnClickListener(v -> uploadImage());

        buttonViewMedicalRecord.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), MedicalRecordsActivity.class));
        });

        return view;
    }

    private void initConfig() {
        Map config = new HashMap();
        config.put("cloud_name", "dd1jxbzf1");
        config.put("api_key", "231995763463355");
        config.put("api_secret", "JwhusLwA3raMTU8diso2Z_PhkR0");
//        config.put("secure", true);
        MediaManager.init(getContext(), config);
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*"); // Select image type pdf/gif/png/jpg
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imageActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> imageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    if (o.getResultCode() == Activity.RESULT_OK) {
                        Intent data = o.getData();
                        if (data != null && data.getData() != null) {
                            imagePath = data.getData();
                            Picasso.get().load(imagePath).into(imageViewRecord);
                        }
                    }
                }
            });

    private void uploadImage() {
        if (imagePath == null) {
            Toast.makeText(getContext(), "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }
        if (editTextImageTitle.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        MediaManager.get().upload(imagePath).callback(new UploadCallback() {
            @Override
            public void onStart(String requestId) {
                Log.d(TAG, "onStart: " + requestId);
            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {
                Log.d(TAG, "onProgress: " + bytes + "/" + totalBytes);
            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                Log.d(TAG, "onSuccess: " + resultData.get("url"));
                String imageUrl = (String) resultData.get("url");
                saveImageUrlToFirebase(imageUrl);
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                Log.d(TAG, "onError: " + error.getDescription());
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {
                Log.d(TAG, "onReschedule: " + error.getDescription());
            }
        }).dispatch();
    }

    private void saveImageUrlToFirebase(String imageUrl) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();
        String recordId = UUID.randomUUID().toString();
        String imageTitle = editTextImageTitle.getText().toString();
        // Thay đổi http:// thành https://
        String httpsUrl = imageUrl.replace("http://", "https://");

        // Tạo object chứa thông tin record
        Map<String, Object> medicalRecord = new HashMap<>();
        medicalRecord.put("title", imageTitle);
        medicalRecord.put("imageUrl", httpsUrl);

        // Đẩy vào "users/userId123/medicalRecords/recordId1"
        databaseRef.child("users")
                .child(userId)
                .child("medicalRecords")
                .child(recordId)
                .setValue(medicalRecord)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Lưu medical record thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lưu thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}