package kr.co.shineware.nlp.pykomoran;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;
import kr.co.shineware.util.common.model.Pair;
import py4j.GatewayServer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * KOMORAN을 Wrapping하는 Class로, Py4J에 의해 불리는 Java-side의 EntryPoint입니다.
 * 직접 실행 시 <code>main</code> 메소드에서 Py4J의 GatewayServer를 생성합니다.
 * <p>
 * 사용법은 아래와 같습니다.
 * <pre>
 *     KomoranEntryPoint kep = new KomoranEntryPoint();
 *     kep.init(MODEL_PATH);
 * </pre>
 *
 * @author      <a href="https://github.com/9bow">9bow</a>
 * @see         kr.co.shineware.nlp.komoran.core.Komoran
 * @see         py4j.GatewayServer
 */
public class KomoranEntryPoint {
    private Komoran komoran = null;
    private KomoranResult result = null;

    public void KomoranEntryPoint() {
    }


    /**
     * 내부 <code>Komoran</code> 객체를 <code>modelPath</code>로 초기화합니다.
     *
     * @param       modelPath 모델이 위치한 절대 경로
     * @exception   FileNotFoundException
     *              modelPath에 모델이 존재하지 않을 시 Exception 발생
     * @see         kr.co.shineware.nlp.komoran.core.Komoran
     */
    public void init(String modelPath) {
        // USING DEFAULT MODEL
        if ("STABLE".equals(modelPath) || "EXP".equals(modelPath)) {
            this.initByModelName(modelPath);
            return;
        }

        // CHECK MODEL PATH
        if (!new File(modelPath).exists()) {
            return;
        }

        try {
            komoran = new Komoran(modelPath);
        }
        // TODO: Modify Komoran throws FileNotFoundException
        catch (Exception e) {
            // FileNotFoundException => Invalid model path
        }
    }


    /**
     * 내부 <code>Komoran</code> 객체가 초기화되었는지 확인합니다.
     *
     * @return      초기화 여부 (boolean)
     * @see         kr.co.shineware.nlp.komoran.core.Komoran
     */
    public boolean isInitialized() {
        if (komoran instanceof Komoran) {
            return true;
        }

        return false;
    }


    /**
     * 내부 <code>Komoran</code> 객체를 기본 <code>modelType</code> 초기화합니다.
     * <code>modelType</code>은 KOMORAN의 DEFAULT_MODEL 타입입니다.
     *
     * @param       modelType DEFAULT_MODEL 종류
     * @see         kr.co.shineware.nlp.komoran.core.Komoran
     */
    public void initByModel(DEFAULT_MODEL modelType) {
        komoran = new Komoran(modelType);
    }


    /**
     * 내부 <code>Komoran</code> 객체를 기본 <code>modelTypeName</code> 초기화합니다.
     * <code>modelTypeName</code>은 KOMORAN의 DEFAULT_MODEL의 이름입니다.
     *
     * @param       modelTypeName DEFAULT_MODEL의 이름
     * @see         kr.co.shineware.nlp.komoran.core.Komoran
     */
    public void initByModelName(String modelTypeName) {
        switch (modelTypeName) {
            case "STABLE":
                komoran = new Komoran(DEFAULT_MODEL.STABLE);
                break;
            case "EXP":
                komoran = new Komoran(DEFAULT_MODEL.EXPERIMENT);
                break;
            default:
                // TODO: throw ModelNotFoundException
        }
    }


    /**
     * 내부 <code>Komoran</code> 객체에 사용자 사전을 적용합니다.
     *
     * @param       userDicPath 사용자 사전이 위치한 절대 경로
     */
    public void setUserDic(String userDicPath) {
        komoran.setUserDic(userDicPath);
    }


    /**
     * 내부 <code>Komoran</code> 객체에 기분석 사전을 적용합니다.
     *
     * @param       fwDicPath 기분석 사전이 위치한 절대 경로
     */
    public void setFWDic(String fwDicPath) {
        komoran.setFWDic(fwDicPath);
    }


    /**
     * 내부 <code>Komoran</code> 객체에 주어진 sentence를 분석하여 내부 <code>KomoranResult</code> 객체에 저장합니다.
     *
     * @param       sentence 분석할 문장
     * @see         kr.co.shineware.nlp.komoran.model.KomoranResult
     */
    public void analyze(String sentence) {
        result = komoran.analyze(sentence);
    }


    /**
     * 내부 <code>KomoranResult</code> 객체로부터 명사류의 형태소만 반환받습니다.
     *
     * @return      분석 결과 중, 명사류의 형태소 List
     * @see         kr.co.shineware.nlp.komoran.model.KomoranResult
     */
    public List<String> getNouns() {
        return result.getNouns();
    }


    /**
     * 내부 <code>KomoranResult</code> 객체로부터 주어진 품사의 형태소들만 반환받습니다.
     *
     * @param       targetPosCollection 품사 List
     * @return      주어진 형태소들에 해당하는 형태소 List
     * @see         kr.co.shineware.nlp.komoran.model.KomoranResult
     */
    public List<String> getMorphesByTags(List<String> targetPosCollection) {
        return result.getMorphesByTags(targetPosCollection);
    }


    /**
     * 내부 <code>KomoranResult</code> 객체로부터 PlainText 형태의 분석 결과를 반환받습니다.
     *
     * @return      전체 형태소 분석 결과의 PlainText
     * @see         kr.co.shineware.nlp.komoran.model.KomoranResult
     */
    public String getPlainText() {
        return result.getPlainText();
    }


    /**
     * 내부 <code>KomoranResult</code> 객체로부터 분석 결과를 <code>Token</code> 형태로 반환받습니다.
     * Python에서 이용할 수 있도록 <code>Token</code> 객체는 Map 객체로 변환하여 제공합니다.
     *
     * @return      형태소 분석 결과의 Map(Token) List
     * @see         kr.co.shineware.nlp.komoran.model.KomoranResult
     */
    public List<Map<String, Object>> getTokenList() {
        // @formatter:off
        return result.getTokenList()
                     .stream()
                     .map(this::convertTokenToMap)
                     .collect(Collectors.toList());
        // @formatter:on
    }


    /**
     * 내부 <code>KomoranResult</code> 객체로부터 분석 결과를 <code>Pair</code> 형태로 반환받습니다.
     * Python에서 이용할 수 있도록 <code>Pair</code> 객체는 Map 객체로 변환하여 제공합니다.
     *
     * @return      형태소 분석 결과의 Map(Pair) List
     * @see         kr.co.shineware.nlp.komoran.model.KomoranResult
     */
    public List<Map<String, String>> getList() {
        // @formatter:off
        return result.getList()
                     .stream()
                     .map(this::convertPairToMap)
                     .collect(Collectors.toList());
        // @formatter:on
    }


    /**
     * <code>Token</code> 객체를 Python에서 이용할 수 있도록 각 Key를 이름으로 갖는 Map 객체로 변환합니다.
     *
     * @param       token Token 객체
     * @return      Map(Token) 객체
     */
    private Map<String, Object> convertTokenToMap(Token token) {
        return new HashMap<String, Object>() {{
            put("morph", token.getMorph());
            put("pos", token.getPos());
            put("beginIndex", token.getBeginIndex());
            put("endIndex", token.getEndIndex());
        }};
    }


    /**
     * <code>Pair</code> 객체를 Python에서 이용할 수 있도록 각 Key를 이름으로 갖는 Map 객체로 변환합니다.
     *
     * @param       pair Pair 객체
     * @return      Map(Pair) 객체
     */
    private Map<String, String> convertPairToMap(Pair pair) {
        return new HashMap<String, String>() {{
            put("first", pair.getFirst().toString());
            put("second", pair.getSecond().toString());
        }};
    }


    /**
     * 직접 실행 시 Py4J의 GatewayServer를 실행합니다.
     *
     * @param       args
     * @see         py4j.GatewayServer
     */
    public static void main(String[] args) {
        // Sample code for testing
        KomoranEntryPoint komoranEntryPoint = new KomoranEntryPoint();
        komoranEntryPoint.initByModel(DEFAULT_MODEL.STABLE);
        komoranEntryPoint.analyze("① 대한민국은 민주공화국이다. ② 대한민국의 주권은 국민에게 있고, 모든 권력은 국민으로부터 나온다.");
        System.out.println(komoranEntryPoint.getTokenList());

        // Codes below are for debugging
        GatewayServer gatewayServer = new GatewayServer(new KomoranEntryPoint(), 25335);
        gatewayServer.start();
    }
}
