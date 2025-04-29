package hcmute.edu.vn.healthtrackerapp.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.healthtrackerapp.adapter.AppointmentAdapter;
import hcmute.edu.vn.healthtrackerapp.R;
import hcmute.edu.vn.healthtrackerapp.model.Appointment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DoctorAppointmentsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DoctorAppointmentsFragment extends Fragment {

    private RecyclerView recyclerAppointments;
    private List<Appointment> appointmentList;
    private AppointmentAdapter appointmentAdapter;

    private DatabaseReference appointmentsRef;
    private String currentUserId;
    private static String doctorEmail;
    Button btnAddAppointment;
    private DatabaseReference usersRef;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DoctorAppointmentsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DoctorAppointmentsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DoctorAppointmentsFragment newInstance(String param1, String param2) {
        DoctorAppointmentsFragment fragment = new DoctorAppointmentsFragment();
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
        View view = inflater.inflate(R.layout.fragment_doctor_appointments, container, false);

        recyclerAppointments = view.findViewById(R.id.recyclerAppointments);

        recyclerAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        appointmentList = new ArrayList<>();
        appointmentAdapter = new AppointmentAdapter(appointmentList, getContext());
        recyclerAppointments.setAdapter(appointmentAdapter);

        btnAddAppointment = view.findViewById(R.id.btnAddAppointment);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        appointmentsRef = FirebaseDatabase.getInstance().getReference("Appointments");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Kiểm tra role
        usersRef.child(currentUserId).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String role = snapshot.getValue(String.class);
                if ("doctor".equalsIgnoreCase(role)) {
                    btnAddAppointment.setVisibility(View.VISIBLE);
                    btnAddAppointment.setOnClickListener(v -> {
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        doctorEmail = auth.getCurrentUser().getEmail();
                        AddAppointmentDialogFragment dialog = AddAppointmentDialogFragment.newInstance(doctorEmail);
                        dialog.show(getChildFragmentManager(), "AddAppointment");
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Không thể kiểm tra vai trò", Toast.LENGTH_SHORT).show();
            }
        });

        loadAppointments();

        return view;
    }

    private void loadAppointments() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        doctorEmail = auth.getCurrentUser().getEmail();
        appointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appointmentList.clear();
                for (DataSnapshot item : snapshot.getChildren()) {
                    Appointment appointment = item.getValue(Appointment.class);
                    if (appointment != null && doctorEmail.equals(appointment.doctorEmail)) {
                        appointmentList.add(appointment);
                    }
                }
                Log.d("Appointments", "Loaded: " + appointmentList.size());
                appointmentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load appointments", Toast.LENGTH_SHORT).show();
            }
        });
    }
}