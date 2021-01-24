package com.dimotim.kubSolver.kubsolver;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.dimotim.kubSolver.Kub;
import com.dimotim.kubSolver.Kub2x2;
import com.dimotim.kubsolver.Solvers;
import com.dimotim.kubsolver.updatecheck.HttpClient;
import com.dimotim.kubsolver.updatecheck.SchedulerProvider;
import com.dimotim.kubsolver.updatecheck.UpdatesUtil;
import com.dimotim.kubsolver.updatecheck.model.CheckResult;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class SolverTest {
    @Test
    public void solversTest() {
        Solvers solvers = new Solvers(){};

        solvers.getKubSolver().solve(new Kub(true));
        solvers.getKub2x2Solver().solve(new Kub2x2(true));
        solvers.getUzorSolver().apply(new Kub(true), new Kub(true));
        solvers.getUzor2x2Solver().apply(new Kub2x2(true), new Kub2x2(true));
    }

    @Test
    public void updateCheckTest(){
        Context appContext = InstrumentationRegistry.getTargetContext();

        CheckResult result = HttpClient.getCheckForUpdateService()
                .getLatestRelease()
                .map(UpdatesUtil::parseCheckResultFromGithubResponse)
                .observeOn(SchedulerProvider.ui())
                .subscribeOn(SchedulerProvider.io())
                .blockingGet();

        assertNotNull(result);
    }


    @Test
    public void qrCodeTest() throws WriterException {
        Bitmap bitmap = new BarcodeEncoder()
                .encodeBitmap("test content", BarcodeFormat.QR_CODE, 400, 400);

        assertNotNull(bitmap);
    }
}
