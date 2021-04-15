package cdc.office.doc.html;

import htmlflow.HtmlView;
import htmlflow.StaticHtml;

public class X {

    public static void main(String[] args) {
        final StaticHtml view = StaticHtml.view(v -> {
            v
             .html()
             .body()
             .__()
             .__();
        });
        view.setPrintStream(System.out).render();

        final StaticHtml view1 = StaticHtml.view();
        final HtmlView<?> body = view1.html().body().__().__();
        view1.addPartial(body);
        view1.setPrintStream(System.out).render();

    }

}