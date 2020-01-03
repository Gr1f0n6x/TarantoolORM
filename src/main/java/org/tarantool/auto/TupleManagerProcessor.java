package org.tarantool.auto;

import com.google.auto.service.AutoService;
import org.tarantool.orm.annotations.Tuple;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("org.tarantool.orm.annotations.Tuple")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public final class TupleManagerProcessor extends BaseProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        info("Start TupleManager processor");

        try {
            info("Generate managers");
            List<TupleMeta> metas = generateTupleManagers(roundEnv);
            info("Generate manager factory");
            generateTupleManagerFactory(metas);
        } catch (IllegalArgumentException | IOException e) {
            error(e.getLocalizedMessage());
            return true;
        }

        return false;
    }

    private List<TupleMeta> generateTupleManagers(RoundEnvironment roundEnv) throws IOException {
        List<TupleMeta> metaList = new ArrayList<>();
        TupleManagerGenerator tupleManagerGenerator = new TupleManagerGenerator();

        for (Element element : roundEnv.getElementsAnnotatedWith(Tuple.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                throw new IllegalArgumentException(String.format("Only classes may be annotated by Tuple: %s", element.getSimpleName()));
            }

            TupleMeta meta = TupleMeta.getInstance((TypeElement) element, typeUtils);
            info("Start to generate new class: %s", meta.className);
            tupleManagerGenerator.generate(filer, typeUtils, meta);
            metaList.add(meta);
        }

        return metaList;
    }

    private void generateTupleManagerFactory(List<TupleMeta> metaList) throws IOException {
        ManagerFactoryGenerator managerFactoryGenerator = new ManagerFactoryGenerator();

        if (!metaList.isEmpty()) {
            managerFactoryGenerator.generate(filer, metaList);
        }
    }
}
