package hcmute.edu.vn.healthtrackerapp.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import hcmute.edu.vn.healthtrackerapp.adapter.AppointmentAdapter;
import hcmute.edu.vn.healthtrackerapp.MakeAppointmentActivity;
import hcmute.edu.vn.healthtrackerapp.R;
import hcmute.edu.vn.healthtrackerapp.model.Appointment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AppointmentsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AppointmentsFragment extends Fragment implements AppointmentUpdateListener {

    private Button btnMakeAppointment;
    private RecyclerView recyclerAppointments;
    private List<Appointment> appointmentList;
    private AppointmentAdapter appointmentAdapter;

    private DatabaseReference appointmentsRef;
    private String currentUserId;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AppointmentsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AppointmentsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AppointmentsFragment newInstance(String param1, String param2) {
        AppointmentsFragment fragment = new AppointmentsFragment();
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
        View view = inflater.inflate(R.layout.fragment_appointments, container, false);

        recyclerAppointments = view.findViewById(R.id.recyclerAppointments);

        recyclerAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        appointmentList = new ArrayList<>();
        appointmentAdapter = new AppointmentAdapter(appointmentList, getContext());
        recyclerAppointments.setAdapter(appointmentAdapter);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        appointmentsRef = FirebaseDatabase.getInstance().getReference("Appointments");

        btnMakeAppointment.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), MakeAppointmentActivity.class));
        });

        loadAppointments();

        return view;
    }

    private void loadAppointments() {
        Calendar calendar = Calendar.getInstance();
        String formattedDate = String.format("%02d/%02d/%04d", calendar.get(Calendar.DATE), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));

        appointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appointmentList.clear();
                for (DataSnapshot item : snapshot.getChildren()) {
                    Appointment appointment = item.getValue(Appointment.class);
                    if (appointment != null && formattedDate.equals(appointment.date)) {
                        appointmentList.add(appointment);
                    }
                }
                appointmentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load appointments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAppointmentUpdated() {
        loadAppointments();
    }
}

