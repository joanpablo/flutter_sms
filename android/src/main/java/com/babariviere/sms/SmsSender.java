package com.babariviere.sms;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SmsManager;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;

import static io.flutter.plugin.common.PluginRegistry.Registrar;
import static io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener;

/**
 * Created by babariviere on 08/03/18.
 */

@TargetApi(Build.VERSION_CODES.DONUT)
class SmsSender implements MethodCallHandler, RequestPermissionsResultListener {
  private static final SmsManager sms = SmsManager.getDefault();
  private final Permissions permissions;
  private final String[] permissionsList = new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE};
  private MethodChannel.Result result;
  private String address;
  private String body;

  SmsSender(Registrar registrar) {
    permissions = new Permissions(registrar.activity());
    registrar.addRequestPermissionsResultListener(this);
  }

  @Override
  public void onMethodCall(MethodCall call, MethodChannel.Result result) {
    this.result = result;
    if (call.method.equals("sendSMS")) {
      address = call.argument("address");
      body = call.argument("body");
      if (address == null) {
        result.error("#02", "missing argument 'address'", null);
      } else if (body == null) {
        result.error("#02", "missing argument 'body'", null);
      } else {
        if (permissions.checkAndRequestPermission(permissionsList, Permissions.SEND_SMS_ID_REQ)) {
          sms.sendTextMessage(address, null, body, null, null);
          result.success(null);
        }
      }
    } else {
      result.notImplemented();
    }
  }

  @Override
  public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    if (requestCode != Permissions.SEND_SMS_ID_REQ) {
      return false;
    }
    boolean isOk = true;
    for (int res: grantResults) {
      if (res != PackageManager.PERMISSION_GRANTED) {
        isOk = false;
        break;
      }
    }
    if (isOk) {
      sms.sendTextMessage(address, null, body, null, null);
      result.success(null);
      return true;
    }
    result.error("#01", "permission denied", null);
    return false;
  }
}
