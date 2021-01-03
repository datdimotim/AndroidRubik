package com.dimotim.kubSolver.kubsolver;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.dimotim.kubSolver.Kub;
import com.dimotim.kubSolver.Kub2x2;
import com.dimotim.kubsolver.Solvers;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class SolverTest {
    @Test
    public void useAppContext() {
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.dimotim.kubSolver.kubsolver", appContext.getPackageName());

        Solvers solvers = Solvers.getSolvers();

        solvers.kubSolver.solve(new Kub(true));
        solvers.kub2x2Solver.solve(new Kub2x2(true));
        solvers.uzorSolver.apply(new Kub(true), new Kub(true));
        solvers.uzor2x2Solver.apply(new Kub2x2(true), new Kub2x2(true));
    }
}
