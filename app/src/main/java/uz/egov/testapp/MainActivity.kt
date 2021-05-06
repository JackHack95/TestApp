package uz.egov.testapp
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import uz.egov.docreader_mrz_nfc.data.json.response.NfcData
import uz.egov.docreader_mrz_nfc.model.NfcConst
import uz.egov.docreader_mrz_nfc.model.NfcConst.FULL_BIRTH_DATE_LABEL
import uz.egov.docreader_mrz_nfc.model.NfcConst.FULL_DOC_NUMBER_LABEL
import uz.egov.docreader_mrz_nfc.model.NfcConst.FULL_EXPIRE_DATE_LABEL
import uz.egov.docreader_mrz_nfc.model.NfcConst.NFC_ERROR_LABEL
import uz.egov.docreader_mrz_nfc.model.NfcConst.NFC_MRZ_REQUEST_CODE
import uz.egov.docreader_mrz_nfc.model.NfcConst.NFC_READER_REQUEST_CODE
import uz.egov.docreader_mrz_nfc.model.models.EDocument
import uz.egov.docreader_mrz_nfc.view.capture.NfcCaptureActivity
import uz.egov.docreader_mrz_nfc.view.nfc.ActivityNfcReader

class MainActivity : AppCompatActivity() {
    var mrzPassportInfo: NfcData?=null
    var chipData: EDocument?=null
    var faceResult:Double=-1.0
    private val PERMISSIONS_REQUEST_CODE = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //MARK: init client credentials
        NfcConst.PD_ALLOW=false
        NfcConst.CLIENT_ID=""
        NfcConst.USER_ID=""
        NfcConst.CLIENT_SECRET=""


        mrzBtn.setOnClickListener {
            requestPermissionForCamera()
        }

        nfcBtn.setOnClickListener{
            openNfcActivity()
        }

        faceBtn.setOnClickListener{
//            openFaceActivity()
        }

    }


    /**
     * #camera permission
     */
    private fun requestPermissionForCamera() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        val isPermissionGranted: Boolean = hasPermissions(
                this,
                *permissions
        )
        if (!isPermissionGranted) {
            showAlertDialog(
                    this,
                    "Camera Permission",
                    "Please give permission for camera usage",
                    "Ok",
                    "","",false
            ) { _, _ ->
                ActivityCompat.requestPermissions(
                        this,
                        permissions,
                        PERMISSIONS_REQUEST_CODE
                )
            }
        } else {
            openCameraActivity()
        }
    }

    private fun openCameraActivity() {
        val intent = Intent(this, NfcCaptureActivity::class.java)
        startActivityForResult(intent, NFC_MRZ_REQUEST_CODE)
    }

    private fun openNfcActivity() {
        val intent = Intent(this, ActivityNfcReader::class.java)
        intent.putExtra(FULL_DOC_NUMBER_LABEL,mrzPassportInfo?.nfcPassportData?.document)
        intent.putExtra(FULL_BIRTH_DATE_LABEL,mrzPassportInfo?.nfcPassportData?.birth_date)
        intent.putExtra(FULL_EXPIRE_DATE_LABEL,mrzPassportInfo?.nfcPassportData?.date_end_document)
        startActivityForResult(intent, NFC_READER_REQUEST_CODE)
    }

//    private fun openFaceActivity() {
//        val intent = Intent(this, AuthenticationActivity::class.java)
//        intent.putExtra(FULL_ISSUE_DATE_LABEL,mrzPassportInfo?.fullPassportData?.date_begin_document)
//        intent.putExtra(FULL_PINFL_LABEL,mrzPassportInfo?.pinfl)
//        intent.putExtra(FULL_DOC_NUMBER_LABEL,mrzPassportInfo?.fullPassportData?.document)
//        startActivityForResult(intent,FULL_FACE_ID_REQUEST_CODE)
//    }


    @SuppressLint("ShowToast")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            NFC_MRZ_REQUEST_CODE -> {
                when (resultCode) {
                    RESULT_OK -> {
                        //passport data from SPC and MIA
                        mrzPassportInfo =NfcConst.MRZ_DATA
                        Log.d("Passport info","Pass info $mrzPassportInfo")
                    }
                    RESULT_CANCELED -> {
                        val error: String = data?.getStringExtra(NFC_ERROR_LABEL) ?: ""
                        if(error.isNotEmpty())
                            Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
                    }
                }
            }
            NFC_READER_REQUEST_CODE -> {
                when (resultCode) {
                    RESULT_OK -> {
                        //passport data from nfc chip of document
                        chipData=NfcConst.NFC_DATA
                        Log.d("Passport info","Pass info $chipData")
                    }
                    RESULT_CANCELED -> {
                        val error: String = data?.getStringExtra(NFC_ERROR_LABEL) ?: ""
                        if(error.isNotEmpty())
                            Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
                    }
                }
            }
//                FULL_FACE_ID_REQUEST_CODE -> {
//                    when (resultCode) {
//                        RESULT_OK -> {
//                            // similarity between selfie and document photo from SPC
//                            faceResult=data!!.getDoubleExtra(FULL_DATA_LABEL, -1.0)
//                            Log.d("Passport info","Pass info $faceResult")
//                        }
//                        RESULT_CANCELED -> {
//                            val error: String = data?.getStringExtra(FULL_ERROR_LABEL) ?: ""
//                            if(error.isNotEmpty())
//                                Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
//                        }
//                    }
//                }
        }
    }


    private fun hasPermissions(context: Context?, vararg permissions: String?): Boolean {
        if (context != null && permissions.isNotEmpty()) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context,
                                permission!!) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    private fun showAlertDialog(
            activity: Activity?,
            title: String,
            message: String,
            positiveButtonText: String,
            negativeButtonText: String,
            neutralButtonText: String,
            isCancelable: Boolean,
            listener: (Any, Any) -> Unit
    ) {
        val dialogBuilder = AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(isCancelable)
        if (positiveButtonText.isNotEmpty()) dialogBuilder.setPositiveButton(
                positiveButtonText,
                listener
        )
        if (negativeButtonText.isNotEmpty()) dialogBuilder.setNegativeButton(
                negativeButtonText,
                listener
        )
        if (neutralButtonText.isNotEmpty()) dialogBuilder.setNeutralButton(
                neutralButtonText,
                listener
        )
        dialogBuilder.show()
    }

}