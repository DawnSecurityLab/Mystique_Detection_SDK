package com.jingdong.dawnslab_vulscanner;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.jingdong.dawnslab_vulscanner.databinding.FragmentSecondBinding;

import java.io.File;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;

    static {
        System.loadLibrary("native-lib");
    }
    public native Map<String, String> stringFromJNI(String dir);

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    // return install time from package manager, or apk file modification time,
    // or null if not found
    public Date getInstallTime(
            PackageManager packageManager, String packageName) {
        return firstNonNull(
                installTimeFromPackageManager(packageManager, packageName),
                apkUpdateTime(packageManager, packageName));
    }

    private Date apkUpdateTime(
            PackageManager packageManager, String packageName) {
        try {
            ApplicationInfo info = packageManager.getApplicationInfo(packageName, 0);
            File apkFile = new File(info.sourceDir);
            return apkFile.exists() ? new Date(apkFile.lastModified()) : null;
        } catch (PackageManager.NameNotFoundException e) {
            return null; // package not found
        }
    }

    private Date installTimeFromPackageManager(
            PackageManager packageManager, String packageName) {
        // API level 9 and above have the "firstInstallTime" field.
        // Check for it with reflection and return if present.
        try {
            PackageInfo info = packageManager.getPackageInfo(packageName, 0);
            //Field field = PackageInfo.class.getField("firstInstallTime");
            Field field = PackageInfo.class.getField("lastUpdateTime");
            long timestamp = field.getLong(info);
            return new Date(timestamp);
        } catch (PackageManager.NameNotFoundException e) {
            return null; // package not found
        } catch (IllegalAccessException e) {
        } catch (NoSuchFieldException e) {
        } catch (IllegalArgumentException e) {
        } catch (SecurityException e) {
        }
        // field wasn't found
        return null;
    }

    private Date firstNonNull(Date... dates) {
        for (Date date : dates)
            if (date != null)
                return date;
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String output="";

        final PackageManager packageManager = getActivity().getPackageManager();
        int flags = PackageManager.GET_META_DATA |
                PackageManager.GET_SHARED_LIBRARY_FILES |
                PackageManager.GET_UNINSTALLED_PACKAGES;
        List<ApplicationInfo> pinfo = packageManager.getInstalledApplications(flags);


        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {

                if ((pinfo.get(i).flags & ApplicationInfo.FLAG_SYSTEM) != 1)
                {

                    PackageManager pm = getActivity().getPackageManager();
                    String str = pinfo.get(i).packageName;

                    String strDateFormat = "yyyy-MM-dd HH:mm:ss";
                    SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
                    boolean need_check = false;
                    boolean hacked = false;

                    try {
                        Date date1 = sdf.parse("2020-09-09 23:59:59"); // the day of android 11.0 release
                        int api_version = Build.VERSION.SDK_INT;

                        if(getInstallTime(pm, str)!=null && getInstallTime(pm, str).after(date1) && api_version == 30){
                            need_check = true;
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                    Map<String, String> ret = stringFromJNI(pinfo.get(i).sourceDir);

                    if( ret.size()>=1 && need_check){

                        for (Map.Entry<String, String> entry : ret.entrySet()) {

                            try{
                                PackageManager pkg = getActivity().getPackageManager();
                                PackageInfo ppinfo = pkg.getPackageInfo(str, 0);

                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            
                            if(getInstallTime(pm, str) !=null && getInstallTime(pm, str).before(new Date(entry.getKey().replaceAll("\n","")))){
                                hacked = true;

                            }
                        }

                        PackageInfo info = null;

                        try {
                            info = pm.getPackageInfo(str,PackageManager.GET_ACTIVITIES);
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }

                        if(hacked)
                        {
                            output += info.applicationInfo.loadLabel(pm).toString() + "\n";
                        }

                    }else{

                    }

                }else {
                }

            }
        }

        // Example of a call to a native method
        TextView tv = binding.sampleText;
        if(output.length()>1){
            tv.setText("以下应用疑似被攻击：\n\n"+output+"\n建议联系：Dawn Security Lab, JD.com 以便进一步确认");
        }else{
            tv.setText("本次共检查"+pinfo.size()+"个应用，未检查出问题项");
        }



        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}