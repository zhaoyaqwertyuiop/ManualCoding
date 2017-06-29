package com.manualcoding.manualcoding;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks{

    private ManualCodingView manualCodingView;
    private CheckBox checkbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        DemoPath dp = new DemoPath(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        setContentView(dp);
        this.manualCodingView = (ManualCodingView) super.findViewById(R.id.manualCodingView);
        this.checkbox = (CheckBox) super.findViewById(R.id.checkbox);

        manualCodingView.setImageResource(R.mipmap.ic_recyclerview_04);
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                manualCodingView.setMasking(isChecked);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clearBtn: // 清空
                manualCodingView.cancelLastMask();
//                manualCodingView.clean();
                break;
            case R.id.saveBtn: // 保存
                getSDPremission();
                break;
            default: break;
        }
    }

    /*********************************************  权限相关  **************************************************/

    private interface PermissionCallback {
        /** 已授权 */
        void granted();

        /** 权限被拒绝 */
        void denied();
    }

    private static final int REQUESTCODE_SD = 100;

    private PermissionCallback permissionCallback = new PermissionCallback() {
        @Override
        public void granted() {
            String name = TimeToStringUtil.format(System.currentTimeMillis(), "yyyyMMddHHmmss") + ".jpg";
            File file;
            if (FileUtil.checkSDCardAvaliable()) {
                file = new File(Environment.getExternalStorageDirectory() + "/" + name);
            } else {
                file = new File(MainActivity.this.getFilesDir() + "/"  + name);
            }
            if (new BitmapFileUtil().saveBitmapFile(file, manualCodingView.getMaskedBitmap())) {
                Toast.makeText(MainActivity.this, "保存成功:" + file.toString(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "保存失败:", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void denied() {
            Toast.makeText(MainActivity.this, "没有读写权限,请授权", Toast.LENGTH_SHORT).show();
        }
    };

    /** 检查GPS权限,有gps权限才获取经纬度 */
    private void getSDPremission() {
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            permissionCallback.granted();
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "读写SD卡权限", REQUESTCODE_SD, perms);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (requestCode == REQUESTCODE_SD) {
            permissionCallback.granted();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (requestCode == REQUESTCODE_SD) {
            permissionCallback.denied();
        }
    }
}
