package com.bignerdranch.android.criminalintent;


import android.Manifest;
import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import android.support.v4.app.ShareCompat;


import android.support.v4.content.FileProvider;
import android.text.Editable;

import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;

import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;


import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.widget.CompoundButton.*;

/**
 * Created by Administrator on 2017-10-11.
 */

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_PHOTO = 2;
    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private Callbacks mCallbacks;

    private CheckBox mSolvedCheckBox;
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mCallButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private File mPhotoFile;
    private ViewTreeObserver mPhotoObserver;

    public interface Callbacks {
        void onCrimeUpdated(Crime crime);
        void onCrimeDeleted(Crime crime);
        void onCrimeAllDeleted(Crime crime);
    }

    public static CrimeFragment newInstance(UUID crimeId){

        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID,crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        {
//            mCrime = new Crime();
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
            UUID crimeId = (UUID)getArguments().getSerializable(ARG_CRIME_ID);
            mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
            mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
        }
    }
    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity())
                .updateCrime(mCrime);
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.fragment_crime,menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete_crime:
                CrimeLab.get(getActivity()).deleteCrime(mCrime);
                if (CrimeLab.get(getActivity()).getCrimes().isEmpty()) {
                    mCallbacks.onCrimeAllDeleted(mCrime);
                } else {
                    mCrime = CrimeLab.get(getActivity()).getCrimes().get(0);
                    mCallbacks.onCrimeDeleted(mCrime); // 这里相当于选中第一个
                    updateCrime(); // 这里面升级了数据层并且更新了列表
                }
                if (getActivity().findViewById(R.id.detail_fragment_container) == null) {
                    getActivity().finish();
                }
//                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        View v = inflater.inflate(R.layout.fragment_crime,container,false);

        mTitleField = (EditText)v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s,int start,int count,int after) {
            }
            @Override
            public void onTextChanged(CharSequence s,int start,int before,int count){
                mCrime.setTitle(s.toString());
                updateCrime();
            }
            @Override
            public void afterTextChanged(Editable s){

            }
        });
        mSolvedCheckBox = (CheckBox)v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
                updateCrime();
            }
        });

        mDateButton = (Button)v.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
//                Intent i =  new Intent(Intent.ACTION_SEND);
//                i.setType("text/plain");
//                i.putExtra(Intent.EXTRA_TEXT,getCrimeReport());
//                i.putExtra(Intent.EXTRA_SUBJECT,
//                        getString(R.string.crime_report_subject));
//                i = Intent.createChooser(i, getString(R.string.send_report));
//                startActivity(i);
                ShareCompat.IntentBuilder sc = ShareCompat.IntentBuilder.from(getActivity());
                sc.setType("text/plain");
                sc.setText(getCrimeReport());
                sc.setSubject(getString(R.string.crime_report_subject));
                sc.createChooserIntent();
                sc.startChooser();
            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
//        pickContact.addCategory(Intent.CATEGORY_HOME);
        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
        mSuspectButton .setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                    //申请权限  第二个参数是一个 数组 说明可以同时申请多个权限
                    requestPermissions( new String[]{android.Manifest.permission.READ_CONTACTS}, 1);

                startActivityForResult(pickContact,REQUEST_CONTACT);
            }
        });
        mCallButton = (Button) v.findViewById(R.id.crime_call);
        mCallButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                Uri number = Uri.parse("tel:"+ mCrime.getNumber());
                Intent i =  new Intent(Intent.ACTION_DIAL,number);
                startActivity(i);
            }
        });


        if(mCrime.getSuspect() != null){
            mSuspectButton.setText(mCrime.getSuspect());
        }

        PackageManager packageManager = getActivity().getPackageManager();
        if(packageManager.resolveActivity(pickContact,PackageManager.MATCH_DEFAULT_ONLY) == null){
            mSuspectButton.setEnabled(false);
        }

        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);

        mPhotoButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "com.bignerdranch.android.criminalintent.fileprovider",mPhotoFile);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT,uri);

                List<ResolveInfo> cameraActivities = getActivity()
                        .getPackageManager().queryIntentActivities(captureImage,
                                PackageManager.MATCH_DEFAULT_ONLY);
                for(ResolveInfo activity : cameraActivities){
                    getActivity().grantUriPermission(activity.activityInfo.packageName,
                            uri,Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                startActivityForResult(captureImage,REQUEST_PHOTO);
            }
        });

        mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                PictureDialogFragment pictureDialogFragment = PictureDialogFragment.newInstance(mPhotoFile.getPath());
                pictureDialogFragment.setTargetFragment(CrimeFragment.this,0);
                pictureDialogFragment.show(manager,"a");
            }
        });
        mPhotoObserver = mPhotoView.getViewTreeObserver();
        mPhotoObserver.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        updatePhotoView(mPhotoView.getWidth(), mPhotoView.getHeight());
                    }
                });
//        updatePhotoView();
        return v;
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        if (data == null) { //判断数据是否为空，就可以解决这个问题
            return;
        }
        if(resultCode != Activity.RESULT_OK){
            return;
        }

        if(requestCode == REQUEST_DATE){
            Date date = (Date)data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
        }
        else if(requestCode == REQUEST_CONTACT && data != null){
            Uri contactUri = data.getData();
            String[] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts._ID
            };

            Cursor c = getActivity().getContentResolver()
                    .query(contactUri,null,null,null,null);

            try{

                if(c.getCount() == 0){
                    return;
                }
                c.moveToFirst();
                String suspect = c.getString(c.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                String suspectID = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));

                Cursor phone = getActivity().getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + suspectID,null,null);
             if (phone.moveToNext()) {
                 String mPhoneNumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        //给当前的Crime对象设置电话号码
                            mCrime.setNumber(mPhoneNumber);
                 }

//   String number = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                Log.d("log", "phone:"+ number);
                Log.d("log", "suspect:"+ suspect);
                Log.d("log", "suspectID:" +  suspectID);
                mCrime.setSuspect(suspect);
                updateCrime();

                mSuspectButton.setText(suspect);
//                mCallButton.setText("CALL:" + number);
            }finally{
                c.close();
            }
        }
        else if(requestCode == REQUEST_PHOTO){
            Uri uri = FileProvider.getUriForFile(getActivity(),
                    "com.bignerdranch.android.criminalintent.fileprovider",
                    mPhotoFile);

            getActivity().revokeUriPermission(uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            updateCrime();
//            updatePhotoView();
        }


    }
    private void updateCrime() {
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
    }
    private void deleteCrime() {
    }

    public void onCrimeAllDeleted(Crime crime) {
    }
    private void updateDate() {
        mDateButton.setText(mCrime.getDate().toString());
    }

    private String getCrimeReport(){
        String solvedString = null;
        if(mCrime.isSolved()){
            solvedString = getString(R.string.crime_report_solved);
        }
        else{
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat,mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if(suspect == null){
            suspect = getString(R.string.crime_report_no_suspect);
        }
        else{
            suspect = getString(R.string.crime_report_suspect);
        }

        String report = getString(R.string.crime_report,mCrime.getTitle(),dateString,solvedString,suspect);

        return report;
    }
    private void updatePhotoView(int width, int height){
        if(mPhotoFile == null || !mPhotoFile.exists()){
            mPhotoView.setImageDrawable(null);
        }
        else{
//            Bitmap bitmap = PictureUtils.getScaledBitmap(
//                    mPhotoFile.getPath(),getActivity());
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(),width,height);
            mPhotoView.setImageBitmap(bitmap);

        }
    }
}
