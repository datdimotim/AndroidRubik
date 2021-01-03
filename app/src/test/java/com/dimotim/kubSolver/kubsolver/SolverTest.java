package com.dimotim.kubSolver.kubsolver;

import com.dimotim.kubSolver.Kub;
import com.dimotim.kubSolver.Kub2x2;
import com.dimotim.kubsolver.Solvers;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class SolverTest {
    @Test
    public void solverTest() {
        Solvers solvers = Solvers.getSolvers();

        solvers.kubSolver.solve(new Kub(true));
        solvers.kub2x2Solver.solve(new Kub2x2(true));
        solvers.uzorSolver.apply(new Kub(true), new Kub(true));
        solvers.uzor2x2Solver.apply(new Kub2x2(true), new Kub2x2(true));
    }
}