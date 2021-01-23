package com.dimotim.kubsolver;


import com.dimotim.kubSolver.Kub;
import com.dimotim.kubSolver.Kub2x2;
import com.dimotim.kubSolver.KubSolver;
import com.dimotim.kubSolver.KubSolverUtils;
import com.dimotim.kubSolver.Solution;
import com.dimotim.kubSolver.solvers.SimpleSolver1;
import com.dimotim.kubSolver.solvers.SimpleSolver2;
import com.dimotim.kubSolver.tables.FullSymTables2x2;
import com.dimotim.kubSolver.tables.SymTables;

import org.androidannotations.annotations.EBean;

import java.util.function.BiFunction;

import lombok.Getter;


@EBean(scope = EBean.Scope.Singleton)
@Getter
public class Solvers {
    private final KubSolver<?,?> kubSolver;
    private final FullSymTables2x2 kub2x2Solver;
    private final BiFunction<Kub, Kub, Solution> uzorSolver;
    private final BiFunction<Kub2x2,Kub2x2, Solution> uzor2x2Solver;

    protected Solvers(){
        kubSolver = new KubSolver<>(SymTables.readTables(), new SimpleSolver1<>(), new SimpleSolver2<>());
        kub2x2Solver = FullSymTables2x2.readTables();
        uzorSolver = KubSolverUtils
                .uzorSolver(kubSolver::solve,
                        new com.dimotim.kubSolver.Kub(false)::apply
                );
        uzor2x2Solver = KubSolverUtils
                .uzorSolver(kub2x2Solver::solve,
                        new Kub2x2(false)::apply
                );
    }
}
