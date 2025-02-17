//---------------------------------------------------------------------------------------
// Copyright (c) 2001-2018 by PDFTron Systems Inc. All Rights Reserved.
// Consult legal.txt regarding legal and license information.
//---------------------------------------------------------------------------------------

package com.pdftron.android.pdfnetsdksamples.samples

import android.content.Context

import com.pdftron.android.pdfnetsdksamples.OutputListener
import com.pdftron.android.pdfnetsdksamples.PDFNetSample
import com.pdftron.android.pdfnetsdksamples.R
import com.pdftron.android.pdfnetsdksamples.util.Utils
import com.pdftron.common.PDFNetException
import com.pdftron.pdf.Convert
import com.pdftron.pdf.DocumentConversion
import com.pdftron.pdf.OfficeToPDFOptions
import com.pdftron.pdf.PDFDoc
import com.pdftron.pdf.PDFNet
import com.pdftron.sdf.SDFDoc

import java.io.File
import java.util.ArrayList

/**
 * The following sample illustrates how to use the PDF.Convert utility class to convert
 * .docx files to PDF
 *
 *
 * This conversion is performed entirely within the PDFNet and has *no* external or
 * system dependencies dependencies -- Conversion results will be the sam whether
 * on Windows, Linux or Android.
 *
 *
 * Please contact us if you have any questions.
 */
class OfficeToPDFTest(context: Context) : PDFNetSample() {

    init {
        try {
            val layoutPluginPath = Utils.copyResourceToTempFolder(context, R.raw.pdftron_layout_resources, false, "pdftron_layout_resources.plugin")
            PDFNet.addResourceSearchPath(layoutPluginPath)
            val layoutSmartPluginPath = Utils.copyResourceToTempFolder(context, R.raw.pdftron_smart_substitution, false, "pdftron_smart_substitution.plugin")
            PDFNet.addResourceSearchPath(layoutSmartPluginPath)
        } catch (e: Exception) {
            mOutputListener!!.println(e.stackTrace)
        }

        setTitle(R.string.sample_officetopdf_title)
        setDescription(R.string.sample_officetopdf_description)
    }

    override fun run(outputListener: OutputListener?) {
        super.run(outputListener)
        mOutputListener = outputListener
        mFileList.clear()
        printHeader(outputListener!!)
        val resFile = Utils.getAssetTempFile(PDFNetSample.INPUT_PATH + "pdftron_smart_substitution.plugin")
        PDFNet.setResourcesPath(resFile!!.parent)

        // first the one-line conversion interface
        simpleDocxConvert("simple-word_2007.docx", "simple-word_2007_a.pdf")

        // then the more flexible line-by-line interface
        flexibleDocxConvert("the_rime_of_the_ancient_mariner.docx", "the_rime_of_the_ancient_mariner.pdf")


        for (file in mFileList) {
            addToFileList(file)
        }
        printFooter(outputListener)
    }

    companion object {

        private var mOutputListener: OutputListener? = null

        private val mFileList = ArrayList<String>()


        fun simpleDocxConvert(inputFilename: String, outputFilename: String) {
            try {

                // perform the conversion with no optional parameters
                val pdfdoc = PDFDoc()
                Convert.officeToPdf(pdfdoc, Utils.getAssetTempFile(PDFNetSample.INPUT_PATH + inputFilename)!!.absolutePath, null)

                // save the result
                pdfdoc.save(Utils.createExternalFile(outputFilename).absolutePath, SDFDoc.SaveMode.INCREMENTAL, null)
                mFileList.add(File(pdfdoc.getFileName()).getName())

                // And we're done!
                mOutputListener!!.println("Done conversion " + Utils.createExternalFile(outputFilename).absolutePath)
            } catch (e: PDFNetException) {
                mOutputListener!!.println("Unable to convert MS Office document, error:")
                e.printStackTrace()
                mOutputListener!!.println(e.getStackTrace())
            }

        }

        fun flexibleDocxConvert(inputFilename: String, outputFilename: String) {
            try {
                val options = OfficeToPDFOptions()
                options.setSmartSubstitutionPluginPath(PDFNetSample.INPUT_PATH)

                // create a conversion object -- this sets things up but does not yet
                // perform any conversion logic.
                // in a multithreaded environment, this object can be used to monitor
                // the conversion progress and potentially cancel it as well
                val conversion = Convert.streamingPdfConversion(
                        Utils.getAssetTempFile(PDFNetSample.INPUT_PATH + inputFilename)!!.absolutePath, options)

                mOutputListener!!.println(inputFilename + ": " + Math.round(conversion.getProgress() * 100.0)
                        + "% " + conversion.getProgressLabel())

                // actually perform the conversion
                while (conversion.getConversionStatus() === DocumentConversion.e_incomplete) {
                    conversion.convertNextPage()
                    mOutputListener!!.println(inputFilename + ": " + Math.round(conversion.getProgress() * 100.0)
                            + "% " + conversion.getProgressLabel())
                }

                if (conversion.tryConvert() === DocumentConversion.e_success) {
                    val num_warnings = conversion.getNumWarnings()

                    // print information about the conversion
                    for (i in 0 until num_warnings) {
                        mOutputListener!!.println("Warning: " + conversion.getWarningString(i))
                    }

                    // save the result
                    val doc = conversion.getDoc()
                    doc.save(Utils.createExternalFile(outputFilename).absolutePath, SDFDoc.SaveMode.INCREMENTAL, null)
                    mFileList.add(File(doc.getFileName()).getName())

                    // done
                    mOutputListener!!.println("Done conversion " + Utils.createExternalFile(outputFilename).absolutePath)
                } else {
                    mOutputListener!!.println("Encountered an error during conversion: " + conversion.getErrorString())
                }
            } catch (e: PDFNetException) {
                mOutputListener!!.println("Unable to convert MS Office document, error:")
                e.printStackTrace()
                mOutputListener!!.println(e.getStackTrace())
            }

        }
    }

}