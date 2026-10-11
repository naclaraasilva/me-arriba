# App – Detecção Inteligente de Quedas e Assistência de Emergência

## 1. Ideia Central

Um aplicativo Android voltado para **ciclistas, motociclistas, idosos e trabalhadores que atuam sozinhos em campo** (entregadores, motoboys, técnicos de manutenção), capaz de **detectar automaticamente quedas ou acidentes** através dos sensores do celular e **acionar ajuda de emergência** mesmo que o usuário esteja inconsciente ou impossibilitado de pedir socorro manualmente.

**Problema real que resolve:** milhões de acidentes com quedas (de moto, bicicleta ou mesmo idosos em casa) só recebem socorro tardiamente porque a vítima não consegue pedir ajuda. O app atua como um "sistema de resposta automática", similar ao que existe em relógios inteligentes, mas usando apenas o hardware do smartphone.

## 2. Sensores Utilizados

| Sensor | Função no app |
|---|---|
| Acelerômetro | Detecta picos bruscos de aceleração (característicos de impacto/queda). |
| Giroscópio | Identifica mudanças abruptas de orientação/rotação do corpo. |
| Sensor de proximidade | Verifica se o celular está no bolso/mochila (ajuda a filtrar falsos positivos). |
| Sensor de luminosidade | Ajusta o app para modo noturno automaticamente, reduzindo distração visual durante o trajeto. |
| Barômetro (opcional, se disponível no aparelho) | Auxilia a estimar variação de altitude em quedas (ex.: de uma escada). |

A combinação **acelerômetro + giroscópio** é o núcleo do algoritmo de detecção de queda, enquanto os demais sensores refinam a precisão e reduzem alarmes falsos.

## 3. Funcionalidades Principais

- **Detecção automática de queda/acidente** com contagem regressiva de 30 segundos para o usuário cancelar (caso seja um falso positivo).
- **Envio automático de alerta** via internet contendo localização em tempo real para:
  - Contatos de emergência cadastrados (SMS/WhatsApp via API ou notificação push).
  - Um servidor backend que pode notificar serviços de resposta.
- **Histórico de trajetos e eventos**, sincronizado na nuvem.
- **Modo "Check-in de segurança":** o app pergunta periodicamente se está tudo bem durante trajetos longos.
- **Integração com API de clima** (ex.: OpenWeatherMap) para alertar sobre condições de risco (chuva, vento forte) antes de sair.
- **Botão de pânico manual**, além da detecção automática.

## 4. Envio dos dados para a API

Será fornecido o endpoint e os parâmetros que serão enviados para a uma API.

## 5. Critérios de avaliação

- Grau de Complexidade
- Interface
- Usabilidade
- Codificação
