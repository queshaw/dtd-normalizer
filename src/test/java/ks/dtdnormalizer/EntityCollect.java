package ks.dtdnormalizer;

import org.apache.xerces.xni.parser.XMLInputSource;

import com.kendallshaw.dtdnormalizer.DtdSerialization;
import com.kendallshaw.dtdnormalizer.EntityReferenceCollector;
import com.kendallshaw.dtdnormalizer.ErrorHandler;
import com.kendallshaw.dtdnormalizer.IdentifierResolver;
import com.kendallshaw.dtdnormalizer.XniConfiguration;

public class EntityCollect {

    public static void main(String[] args) throws Exception {
        XniConfiguration configuration = new XniConfiguration();
        DtdSerialization ser = new DtdSerialization();
        EntityReferenceCollector erc =
            new EntityReferenceCollector(configuration);
        IdentifierResolver irc =
            new IdentifierResolver("src/test/resources/entities-catalog.xml;src/test/resources/entities/catalog.xml");
        configuration.setEntityResolver(irc);
        configuration.setErrorHandler(new ErrorHandler(ser));
        configuration.initialize();
        XMLInputSource xis = new XMLInputSource(null, "src/test/resources/entities/test.xml", null);
        configuration.parse(xis);
//        for (String name : erc.entityNames())
//            System.out.println("entity: " + name);
    }
}
