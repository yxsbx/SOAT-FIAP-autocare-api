package br.com.autocarehub.application.port.out;

import br.com.autocarehub.domain.model.DemoLead;
import java.util.List;

public interface DemoLeadRepository {

    DemoLead save(DemoLead demoLead);

    List<DemoLead> findAll();
}
