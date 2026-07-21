# Optional Science Entry-Point Integration

The existing team-owned Menu/Category code is intentionally not modified. Add one of these snippets in the screen that should open the Science module.

## From an Activity

```java
import android.content.Intent;
import com.example.aimentor.science.presentation.ScienceActivity;

scienceButton.setOnClickListener(view ->
        startActivity(new Intent(this, ScienceActivity.class)));
```

## From a Fragment

```java
import android.content.Intent;
import com.example.aimentor.science.presentation.ScienceActivity;

scienceButton.setOnClickListener(view ->
        startActivity(new Intent(requireContext(), ScienceActivity.class)));
```

## From a RecyclerView adapter

Prefer sending a click callback to the owning Activity/Fragment instead of launching navigation directly in the adapter:

```java
public interface OnScienceClickListener {
    void onOpenScience();
}
```

The Activity/Fragment then launches `ScienceActivity`. This keeps navigation ownership outside the adapter and avoids tightly coupling a general category adapter to one subject module.

## Important

- Do not add another launcher Activity.
- Do not duplicate Science screens inside Category.
- Do not copy API keys into this integration code.
- Keep the existing team navigation design; only connect its Science card/button to the registered Activity.
