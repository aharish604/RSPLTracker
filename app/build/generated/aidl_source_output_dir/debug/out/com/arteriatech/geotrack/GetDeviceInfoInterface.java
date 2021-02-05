/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package com.arteriatech.geotrack;
// Declare any non-default types here with import statements

public interface GetDeviceInfoInterface extends android.os.IInterface
{
  /** Default implementation for GetDeviceInfoInterface. */
  public static class Default implements com.arteriatech.geotrack.GetDeviceInfoInterface
  {
    /**
         * Demonstrates some basic types that you can use as parameters
         * and return values in AIDL.
         */
    @Override public java.lang.String getSerialNumber() throws android.os.RemoteException
    {
      return null;
    }
    @Override public int getVersionCodes() throws android.os.RemoteException
    {
      return 0;
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.arteriatech.geotrack.GetDeviceInfoInterface
  {
    private static final java.lang.String DESCRIPTOR = "com.arteriatech.geotrack.GetDeviceInfoInterface";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.arteriatech.geotrack.GetDeviceInfoInterface interface,
     * generating a proxy if needed.
     */
    public static com.arteriatech.geotrack.GetDeviceInfoInterface asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.arteriatech.geotrack.GetDeviceInfoInterface))) {
        return ((com.arteriatech.geotrack.GetDeviceInfoInterface)iin);
      }
      return new com.arteriatech.geotrack.GetDeviceInfoInterface.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
        case TRANSACTION_getSerialNumber:
        {
          data.enforceInterface(descriptor);
          java.lang.String _result = this.getSerialNumber();
          reply.writeNoException();
          reply.writeString(_result);
          return true;
        }
        case TRANSACTION_getVersionCodes:
        {
          data.enforceInterface(descriptor);
          int _result = this.getVersionCodes();
          reply.writeNoException();
          reply.writeInt(_result);
          return true;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements com.arteriatech.geotrack.GetDeviceInfoInterface
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      /**
           * Demonstrates some basic types that you can use as parameters
           * and return values in AIDL.
           */
      @Override public java.lang.String getSerialNumber() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.lang.String _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getSerialNumber, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getSerialNumber();
          }
          _reply.readException();
          _result = _reply.readString();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public int getVersionCodes() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getVersionCodes, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getVersionCodes();
          }
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      public static com.arteriatech.geotrack.GetDeviceInfoInterface sDefaultImpl;
    }
    static final int TRANSACTION_getSerialNumber = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_getVersionCodes = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    public static boolean setDefaultImpl(com.arteriatech.geotrack.GetDeviceInfoInterface impl) {
      if (Stub.Proxy.sDefaultImpl == null && impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static com.arteriatech.geotrack.GetDeviceInfoInterface getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }
  /**
       * Demonstrates some basic types that you can use as parameters
       * and return values in AIDL.
       */
  public java.lang.String getSerialNumber() throws android.os.RemoteException;
  public int getVersionCodes() throws android.os.RemoteException;
}
