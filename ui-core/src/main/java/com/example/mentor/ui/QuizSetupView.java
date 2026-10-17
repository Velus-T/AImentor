package com.example.mentor.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.*;
import java.util.List;
import com.example.mentor.data.QuizBank;
import com.example.mentor.data.QuizCard;

/** Common creation controls. Source adapters supply stable scope/level values by position. */
public final class QuizSetupView extends LinearLayout {
    public final Spinner scope, level, count;
    public final EditText topic;
    public final Button create, bank;
    public final TextView status;
    private int[] counts;
    public interface Selection { void open(QuizBank.Set set, List<QuizCard> questions); }
    public QuizSetupView(Context c) { this(c,null); }
    public QuizSetupView(Context c,AttributeSet a) {
        super(c,a); setOrientation(VERTICAL);
        scope=spinner(R.string.mentor_quiz_scope); level=spinner(R.string.mentor_quiz_level);
        label(R.string.mentor_quiz_topic);
        topic=new EditText(c); topic.setMinHeight(dp(52)); topic.setSingleLine(false); topic.setMaxLines(4);
        topic.setTextColor(getResources().getColor(R.color.mentor_quiz_text,c.getTheme()));
        addView(topic,new LayoutParams(-1,-2)); count=spinner(R.string.mentor_quiz_count);
        label(R.string.mentor_quiz_rules);
        create=new Button(c); create.setAllCaps(false); create.setMinHeight(dp(52)); create.setText(R.string.mentor_quiz_create);
        addView(create,new LayoutParams(-1,-2));
        bank=new Button(c); bank.setAllCaps(false); bank.setMinHeight(dp(52)); bank.setText(R.string.mentor_quiz_bank);
        addView(bank,new LayoutParams(-1,-2));
        status=new TextView(c); status.setTextColor(getResources().getColor(R.color.mentor_quiz_text,c.getTheme()));
        status.setPadding(0,dp(8),0,dp(8)); status.setAccessibilityLiveRegion(ACCESSIBILITY_LIVE_REGION_POLITE);
        addView(status,new LayoutParams(-1,-2));
    }
    private void label(int text) {
        TextView view=new TextView(getContext()); view.setText(text); view.setTextSize(14);
        view.setTextColor(getResources().getColor(R.color.mentor_quiz_secondary,getContext().getTheme()));
        view.setPadding(0,dp(10),0,dp(4)); addView(view);
    }
    private Spinner spinner(int label) { label(label); Spinner view=new Spinner(getContext()); view.setMinimumHeight(dp(52)); addView(view,new LayoutParams(-1,-2)); return view; }
    public void configure(String[] scopes,String[] levels,int[] counts) {
        this.counts=counts.clone(); scope.setAdapter(adapter(scopes)); level.setAdapter(adapter(levels));
        String[] names=new String[counts.length]; int defaultIndex=0;
        for(int i=0;i<counts.length;i++) { names[i]=String.valueOf(counts[i]); if(counts[i]==5) defaultIndex=i; }
        count.setAdapter(adapter(names)); count.setSelection(defaultIndex);
    }
    private ArrayAdapter<String> adapter(String[] names) {
        ArrayAdapter<String> adapter=new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_item,names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); return adapter;
    }
    public int count() { return counts[Math.max(0,count.getSelectedItemPosition())]; }
    public String topic() { return topic.getText().toString().trim(); }
    public void loading(boolean loading) {
        create.setEnabled(!loading); bank.setEnabled(!loading);
        scope.setEnabled(!loading); level.setEnabled(!loading); count.setEnabled(!loading); topic.setEnabled(!loading);
        create.setText(loading?R.string.mentor_quiz_creating:R.string.mentor_quiz_create);
        status.setText(loading?R.string.mentor_quiz_creating:R.string.mentor_quiz_rules);
    }
    public void error() { loading(false); status.setText(R.string.mentor_quiz_create_error); create.setText(R.string.mentor_quiz_retry_create); }
    public void attachBank(int userId,String subject,Selection selection) {
        bank.setOnClickListener(v -> {
            try(QuizBank repository=new QuizBank(getContext())) {
                List<QuizBank.Set> sets=repository.list(userId,subject);
                if(sets.isEmpty()) { status.setText(R.string.mentor_quiz_bank_empty); return; }
                String[] labels=new String[sets.size()];
                for(int i=0;i<sets.size();i++) {
                    QuizBank.Set set=sets.get(i);
                    labels[i]=set.topic+"\n"+set.scope+" · "+set.level+" · "+getContext().getString(R.string.mentor_quiz_bank_count,set.count);
                }
                new android.app.AlertDialog.Builder(getContext()).setTitle(R.string.mentor_quiz_bank)
                        .setItems(labels,(dialog,index)-> {
                            try(QuizBank db=new QuizBank(getContext())) {
                                selection.open(sets.get(index),db.questions(userId,sets.get(index).id));
                                status.setText(R.string.mentor_quiz_bank_source);
                            } catch(RuntimeException error) { status.setText(R.string.mentor_quiz_bank_error); }
                        }).setNegativeButton(android.R.string.cancel,null).show();
            } catch(RuntimeException error) { status.setText(R.string.mentor_quiz_bank_error); }
        });
    }
    public Bundle state() {
        Bundle state=new Bundle(); state.putInt("scope",scope.getSelectedItemPosition()); state.putInt("level",level.getSelectedItemPosition());
        state.putInt("count",count.getSelectedItemPosition()); state.putString("topic",topic()); return state;
    }
    public void restore(Bundle state) {
        if(state==null)return;
        scope.setSelection(Math.max(0,Math.min(scope.getCount()-1,state.getInt("scope"))));
        level.setSelection(Math.max(0,Math.min(level.getCount()-1,state.getInt("level"))));
        count.setSelection(Math.max(0,Math.min(count.getCount()-1,state.getInt("count")))); topic.setText(state.getString("topic",""));
    }
    private int dp(int value) { return Math.round(value*getResources().getDisplayMetrics().density); }
}
