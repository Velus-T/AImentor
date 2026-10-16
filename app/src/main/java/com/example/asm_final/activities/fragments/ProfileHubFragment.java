package com.example.asm_final.activities.fragments;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.net.Uri;
import androidx.fragment.app.Fragment;
import com.example.asm_final.R;
public final class ProfileHubFragment extends Fragment {
 private com.example.asm_final.utils.AsyncActionRunner actions;
 @Override public View onCreateView(LayoutInflater inflater,ViewGroup parent,Bundle state) {return inflater.inflate(R.layout.fragment_profile_hub,parent,false);}
 @Override public void onViewCreated(View view,Bundle state) {
  String username=getArguments()==null?"":getArguments().getString("USERNAME","");
  ((TextView)view.findViewById(R.id.hub_name)).setText(username);
  if(getChildFragmentManager().findFragmentById(R.id.profile_settings_container)==null) {
   SettingsFragment settings=new SettingsFragment();Bundle args=new Bundle();args.putString("USERNAME",username);args.putBoolean("embedded",true);settings.setArguments(args);
   getChildFragmentManager().beginTransaction().replace(R.id.profile_settings_container,settings).commitNow();
  }
  com.example.asm_final.AppServices services=((com.example.asm_final.AiMentorApplication)requireActivity().getApplication()).services();
  actions=new com.example.asm_final.utils.AsyncActionRunner(services.io(),task->new android.os.Handler(android.os.Looper.getMainLooper()).post(task));
  actions.submit(()->services.profiles().load(username),profile->{if(profile==null)return;
   if(profile.fullName!=null&&!profile.fullName.isEmpty())((TextView)view.findViewById(R.id.hub_name)).setText(profile.fullName);
   if(profile.avatar!=null&&!profile.avatar.isEmpty())try {((ImageView)view.findViewById(R.id.hub_avatar)).setImageURI(Uri.parse(profile.avatar));}catch(RuntimeException ignored){}
  },error->{});
 }
 @Override public void onDestroyView() {if(actions!=null)actions.close();super.onDestroyView();}
}
