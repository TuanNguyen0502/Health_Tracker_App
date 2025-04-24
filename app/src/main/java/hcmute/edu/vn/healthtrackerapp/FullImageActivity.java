package hcmute.edu.vn.healthtrackerapp;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.squareup.picasso.Picasso;

public class FullImageActivity extends AppCompatActivity {

    ImageView fullImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_full_image);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fullImageView = findViewById(R.id.fullImageView);

        String imageUrl = getIntent().getStringExtra("imageUrl");

        if (imageUrl != null) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_health)
                    .into(fullImageView);
        }
    }
}