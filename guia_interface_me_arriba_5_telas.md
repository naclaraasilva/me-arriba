# Me arriba — Guia de implementação da interface (MVP)

## 1. Objetivo

Reproduzir uma interface Android moderna, em português brasileiro, inspirada na referência visual Me arriba (tema escuro, cartões arredondados, destaque verde para segurança e vermelho para emergência), **sem implementar telas desnecessárias**. O foco é detectar possíveis quedas pelos sensores do smartphone e permitir solicitar ajuda.

Este documento descreve o **MVP com 5 telas essenciais**. Priorize fidelidade visual, navegação funcional e estados coerentes; não simule envio real de alertas como se tivesse ocorrido.

## 2. Tecnologia sugerida

- Android nativo: **Kotlin + Jetpack Compose + Material 3**.
- Navegação: Navigation Compose.
- Estado: ViewModel + StateFlow.
- Sensores: `SensorManager` (acelerômetro e giroscópio); proximidade e barômetro opcionais, conforme disponibilidade.
- Localização: APIs de localização do Android, mediante permissões.
- Persistência local: DataStore para preferências e Room para persistência do histórico.
- Integração de emergência: cliente HTTP para endpoint/API fornecido posteriormente.

Não presumir que o dispositivo tem todos os sensores. Não prometer detecção infalível nem notificação automática sem implementação e validação.

## 3. Design system

### Paleta de cores (referência aproximada)

| Uso | Cor |
|---|---|
| Fundo principal | `#0B141C` |
| Superfícies/cartões | `#142430` |
| Superfície elevada | `#1B303F` |
| Texto principal | `#F7F9FC` |
| Texto secundário | `#A9BAC6` |
| Segurança/ativo | `#20C978` |
| Emergência/alerta | `#E63946` |
| Ação secundária | `#247DE8` |
| Divisórias | `#2A3B47` |

- Tipografia: Material 3 com fonte sans-serif; títulos em semibold/bold.
- Margens laterais: 20 dp; espaçamento entre cartões: 12–16 dp.
- Cartões: cantos de 18–24 dp; botões grandes: 14–20 dp.
- Alvos de toque de pelo menos 48 dp; contraste adequado e suporte a fontes maiores.
- Ícones vetoriais consistentes; evitar excesso de efeitos, brilhos ou gradientes.
- Barra de status e navegação adaptadas ao tema escuro.

## 4. Telas essenciais

### Tela 1 — Início / monitoramento

**Objetivo:** concentrar o status do aplicativo e o acesso à ajuda.

Elementos, de cima para baixo:
1. Cabeçalho com marca **Me arriba** (Safe em branco, Ride em vermelho) e ícone de configurações.
2. Grande indicador circular com escudo verde e texto **Monitoramento ativo** ou, quando desligado, **Monitoramento inativo**.
3. Subtítulo: **Detectando quedas e acidentes** apenas quando o monitoramento estiver efetivamente ativo.
4. Cartão **Sua localização** com endereço resumido ou mensagem **Localização indisponível**.
5. Cartões compactos: **Modo de transporte** (ex.: Motocicleta) e **Clima atual** (somente se houver dados disponíveis; caso contrário, estado indisponível).
6. Controle de **Iniciar/Parar monitoramento** com estado visual inequívoco.
7. Botão vermelho destacado **BOTÃO DE PÂNICO**; exigir pressão prolongada ou confirmação para evitar acionamento acidental.
8. Navegação simples: **Início** e **Histórico**. Configurações pelo ícone superior.

**Estados:** inicializando, ativo, inativo, sensor indisponível, permissão pendente e erro.

### Tela 2 — Possível queda detectada

**Objetivo:** permitir cancelar falso positivo antes de disparar um alerta.

Elementos:
1. Fundo escuro com destaque vermelho.
2. Título **Possível queda detectada**.
3. Pergunta **Você sofreu uma queda ou acidente?**
4. Contador circular vermelho de **30 segundos**.
5. Botão principal **Não, estou bem** — cancela o alerta, registra o falso positivo no histórico e retorna ao monitoramento.
6. Botão secundário **Preciso de ajuda** — tenta enviar o alerta imediatamente.

**Regras:** o contador inicia após detecção válida; ao chegar a zero, iniciar tentativa de envio; impedir disparos duplicados; tratar perda de conexão e permissões. Para demonstração, permitir acionar um evento de queda simulado, claramente identificado como teste.

### Tela 3 — Status do alerta

**Objetivo:** informar o resultado real da tentativa de emergência.

Elementos:
1. Indicador de estado: **Enviando alerta…**, **Alerta enviado** ou **Falha no envio**.
2. Texto explicativo de acordo com o estado.
3. Cartão com status individual de **API/servidor**, **contatos de emergência** e **localização**.
4. Localização resumida, se disponível, e ação **Compartilhar localização** quando suportada.
5. Ações **Tentar novamente** em caso de falha e **Voltar ao início**.

**Importante:** sucesso HTTP da API não prova que o contato recebeu a mensagem. Mostrar **Enviado à API** quando essa for a única confirmação. Não exibir check verde de entrega aos contatos sem retorno verificável.

### Tela 4 — Configurações

**Objetivo:** configurar os recursos necessários ao funcionamento.

Elementos:
1. Título **Configurações** e ação de voltar.
2. Seção **Contatos de emergência**: cadastrar, editar e remover contato (nome, telefone e/ou canal suportado pela API).
3. Seção **Monitoramento**: habilitar/desabilitar, visualizar disponibilidade dos sensores e permissões.
4. Seção **Modo de transporte**: motocicleta, bicicleta, caminhada/outro (se necessário para o projeto).
5. Seção **Check-in de segurança**: ativar/desativar e definir intervalo; usar **diálogo/modal**, sem criar tela própria.
6. Seção **Notificações e localização**: status e acesso às configurações de permissões do Android.

Salvar preferências localmente. Não solicitar permissões desnecessárias no primeiro acesso: solicitar no momento em que forem necessárias.

## 5. Tela 5 — Histórico de eventos (obrigatória)

**Objetivo:** permitir consultar todos os eventos registrados durante o uso do aplicativo.

**Interface:** título **Histórico de eventos**, filtros horizontais, cartões com ícone de estado, tipo do evento, data, hora e resultado. Ao tocar em um evento, exibir detalhes em um diálogo, sem criar outra tela.

- Lista cronológica com data/hora, tipo e resultado: queda detectada, falso positivo, alerta solicitado/enviado/falhou, check-in.
- Filtros simples: **Todos**, **Quedas** e **Alertas**.
- Estado vazio: **Nenhum evento registrado**.
- Persistir os eventos localmente com Room, mantendo-os após fechar o aplicativo; sincronização na nuvem é expansão futura. Registrar também o status de tentativa, sucesso ou falha de envio, sem confundir aceite da API com entrega aos contatos.

## 6. Recursos sem tela própria

- **Check-in de segurança:** diálogo sobre a tela atual com **Sim, estou bem** e **Preciso de ajuda**; tratar ausência de resposta conforme regra definida no projeto, sem presumir envio automático.
- **Clima:** cartão discreto na tela inicial; avisos climáticos apenas quando integração estiver implementada.
- **Mapa/localização em tempo real:** não criar tela exclusiva no MVP; mostrar resumo da localização no status do alerta e permitir compartilhamento por recurso existente, se disponível.

## 7. Fluxos de navegação

```text
Início
 ├── Configurações
 ├── Histórico
 ├── Botão de pânico ───────────────> Status do alerta
 └── Sensor identifica possível queda
       └── Possível queda detectada (30 s)
             ├── Não, estou bem ───> Início
             ├── Preciso de ajuda ─> Status do alerta
             └── Tempo esgotado ───> Status do alerta
```

## 8. Comportamentos técnicos e de segurança

- O algoritmo de queda deve combinar **acelerômetro + giroscópio**, com calibração e testes; proximidade/barômetro são opcionais.
- Separar **UI**, **detecção de sensores** e **serviço de envio de alertas**.
- Não afirmar que o monitoramento permanece ativo em segundo plano sem implementar o mecanismo Android apropriado, notificações e permissões aplicáveis.
- Mostrar claramente quando GPS, internet, sensores ou permissões estiverem indisponíveis.
- Não enviar SMS/WhatsApp diretamente sem canal/API realmente configurado.
- Nunca tratar simulação como envio real. Incluir modo de teste para demonstrar detecção e estados da interface.
- Proteger dados pessoais dos contatos e localização; coletar apenas o necessário.
- A detecção pelo smartphone não substitui serviços de emergência nem garante socorro.

## 9. Ordem de implementação

1. Criar tema, cores, componentes reutilizáveis e navegação.
2. Construir Início com dados simulados **explicitamente identificados**.
3. Construir tela de queda com contador funcional e cancelamento.
4. Construir tela de status do alerta com estados carregando/sucesso/erro.
5. Construir Configurações e persistir contatos/preferências.
6. Integrar sensores, permissões, localização e endpoint real.
7. Testar fluxos completos, erros e funcionamento em segundo plano.
8. Implementar Histórico com Room, registrar eventos dos fluxos de queda, alerta e check-in, e validar filtros e persistência.

## 10. Critérios de aceitação

- [ ] Cinco telas essenciais com aparência consistente com a referência Me arriba.
- [ ] Navegação sem rotas quebradas.
- [ ] Histórico acessível pela barra inferior, com filtros e estado vazio.
- [ ] Eventos de queda, cancelamento, alerta e check-in persistem após reiniciar o app.
- [ ] Início reflete o estado real do monitoramento.
- [ ] Contador de 30 s funciona e pode ser cancelado.
- [ ] Botão de pânico exige confirmação/pressão prolongada.
- [ ] Estado de envio diferencia tentativa, confirmação da API e falha.
- [ ] Contatos e preferências são persistidos.
- [ ] Sensores/permissões indisponíveis possuem tratamento visível.
- [ ] Check-in usa modal, sem tela adicional.
- [ ] Interface se adapta a diferentes tamanhos de tela e acessibilidade.

## 11. Prompt para a IA implementadora

> Implemente o MVP Android **Me arriba** seguindo rigorosamente este guia e a imagem de referência fornecida. Use Kotlin, Jetpack Compose e Material 3 se o projeto ainda não tiver outra stack definida. Crie **5 telas essenciais**: Início, Possível Queda Detectada, Status do Alerta, Configurações e Histórico de Eventos. O Histórico é obrigatório, acessível pela navegação inferior, com filtros Todos/Quedas/Alertas, estado vazio e armazenamento local persistente usando Room. Registre quedas, falsos positivos, tentativas e resultados de alertas e check-ins. Check-in é modal; clima é um cartão na tela inicial; não crie tela separada de mapa. Reproduza o tema escuro, cartões arredondados, cores e hierarquia visual. Implemente navegação, contador de 30 segundos, estados de envio e persistência de configurações. Separe componentes e lógica. Não altere funcionalidades existentes sem necessidade e não declare alertas enviados sem confirmação. Quando faltar API ou integração, use mocks identificados como modo de demonstração. Entregue código organizado, instruções de execução e lista clara do que ainda depende de integração real.
