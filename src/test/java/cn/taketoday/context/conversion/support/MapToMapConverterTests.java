/*
 * Original Author -> 杨海健 (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © TODAY & 2017 - 2021 All Rights Reserved.
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see [http://www.gnu.org/licenses/]
 */

package cn.taketoday.context.conversion.support;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.taketoday.context.conversion.ConversionFailedException;
import cn.taketoday.context.conversion.ConverterNotFoundException;
import cn.taketoday.context.utils.DefaultMultiValueMap;
import cn.taketoday.context.utils.GenericDescriptor;
import cn.taketoday.context.utils.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Keith Donald
 * @author Phil Webb
 * @author Juergen Hoeller
 * @author TODAY 2021/3/26 10:44
 * @since 3.0
 */
public class MapToMapConverterTests {

  private final DefaultConversionService conversionService = new DefaultConversionService();

  @Before
  public void setUp() {
    conversionService.addConverters(new MapToMapConverter(conversionService));
  }

  @Test
  public void scalarMap() throws Exception {
    Map<String, String> map = new HashMap<>();
    map.put("1", "9");
    map.put("2", "37");
    GenericDescriptor targetType = new GenericDescriptor(getClass().getField("scalarMapTarget"));

    assertThat(conversionService.canConvert(HashMap.class, targetType)).isTrue();
    try {
      conversionService.convert(map, targetType);
    }
    catch (ConversionFailedException ex) {
      assertThat(ex.getCause() instanceof ConverterNotFoundException).isTrue();
    }

    conversionService.addConverters(new IntegerConverter(int.class),
                                    new IntegerConverter(Integer.class));
    assertThat(conversionService.canConvert(HashMap.class, targetType)).isTrue();

    Map<Integer, Integer> result = conversionService.convert(map, targetType);
    assertThat(map.equals(result)).isFalse();
    assertThat((int) result.get(1)).isEqualTo(9);
    assertThat((int) result.get(2)).isEqualTo(37);
  }

  @Test
  public void scalarMapNotGenericTarget() throws Exception {
    Map<String, String> map = new HashMap<>();
    map.put("1", "9");
    map.put("2", "37");

    assertThat(conversionService.canConvert(Map.class, Map.class)).isTrue();
    assertThat((Map<?, ?>) conversionService.convert(map, Map.class)).isSameAs(map);
  }

  @Test
  public void scalarMapNotGenericSourceField() throws Exception {
    Map<String, String> map = new HashMap<>();
    map.put("1", "9");
    map.put("2", "37");
    GenericDescriptor sourceType = new GenericDescriptor(getClass().getField("notGenericMapSource"));
    GenericDescriptor targetType = new GenericDescriptor(getClass().getField("scalarMapTarget"));

    assertThat(conversionService.canConvert(Map.class, targetType)).isTrue();
    try {
      conversionService.convert(map, targetType);
    }
    catch (ConversionFailedException ex) {
      assertThat(ex.getCause() instanceof ConverterNotFoundException).isTrue();
    }

    conversionService.addConverters(new IntegerConverter(int.class),
                                    new IntegerConverter(Integer.class));

    assertThat(conversionService.canConvert(Map.class, targetType)).isTrue();

    Map<Integer, Integer> result = conversionService.convert(map, targetType);
    assertThat(map.equals(result)).isFalse();
    assertThat((int) result.get(1)).isEqualTo(9);
    assertThat((int) result.get(2)).isEqualTo(37);
  }

  @Test
  public void collectionMap() throws Exception {
    Map<String, List<String>> map = new HashMap<>();
    map.put("1", Arrays.asList("9", "12"));
    map.put("2", Arrays.asList("37", "23"));
    GenericDescriptor sourceType = GenericDescriptor.forObject(map);
    GenericDescriptor targetType = new GenericDescriptor(getClass().getField("collectionMapTarget"));

    assertThat(conversionService.canConvert(sourceType.getType(), targetType)).isTrue();
    try {
      conversionService.convert(map, targetType);
    }
    catch (ConversionFailedException ex) {
      assertThat(ex.getCause() instanceof ConverterNotFoundException).isTrue();
    }

    conversionService.addConverters(new CollectionToCollectionConverter(conversionService));
    conversionService.addConverters(new IntegerConverter(int.class),
                                    new IntegerConverter(Integer.class));

    assertThat(conversionService.canConvert(sourceType.getType(), targetType)).isTrue();

    Map<Integer, List<Integer>> result = conversionService.convert(map, targetType);
    assertThat(map.equals(result)).isFalse();
    assertThat(result.get(1)).isEqualTo(Arrays.asList(9, 12));
    assertThat(result.get(2)).isEqualTo(Arrays.asList(37, 23));
  }

  @Test
  public void collectionMapSourceTarget() throws Exception {
    Map<String, List<String>> map = new HashMap<>();
    map.put("1", Arrays.asList("9", "12"));
    map.put("2", Arrays.asList("37", "23"));
    GenericDescriptor sourceType = new GenericDescriptor(getClass().getField("sourceCollectionMapTarget"));
    GenericDescriptor targetType = new GenericDescriptor(getClass().getField("collectionMapTarget"));

//    assertThat(conversionService.canConvert(sourceType.getType(), targetType)).isFalse();
//    assertThatExceptionOfType(ConverterNotFoundException.class)
//            .isThrownBy(() -> conversionService.convert(map, targetType));

    conversionService.addConverter(new CollectionToCollectionConverter(conversionService));
    conversionService.addConverters(new IntegerConverter(int.class),
                                    new IntegerConverter(Integer.class));

    assertThat(conversionService.canConvert(sourceType.getType(), targetType)).isTrue();

    Map<Integer, List<Integer>> result = conversionService.convert(map, targetType);
    assertThat(map.equals(result)).isFalse();
    assertThat(result.get(1)).isEqualTo(Arrays.asList(9, 12));
    assertThat(result.get(2)).isEqualTo(Arrays.asList(37, 23));
  }

  @Test
  public void collectionMapNotGenericTarget() throws Exception {
    Map<String, List<String>> map = new HashMap<>();
    map.put("1", Arrays.asList("9", "12"));
    map.put("2", Arrays.asList("37", "23"));

    assertThat(conversionService.canConvert(Map.class, Map.class)).isTrue();
    assertThat((Map<?, ?>) conversionService.convert(map, Map.class)).isSameAs(map);
  }

  @Test
  public void collectionMapNotGenericTargetCollectionToObjectInteraction() throws Exception {
    Map<String, List<String>> map = new HashMap<>();
    map.put("1", Arrays.asList("9", "12"));
    map.put("2", Arrays.asList("37", "23"));
    conversionService.addConverter(new CollectionToCollectionConverter(conversionService));
    conversionService.addConverter(new CollectionToObjectConverter(conversionService));

    assertThat(conversionService.canConvert(Map.class, Map.class)).isTrue();
    assertThat((Map<?, ?>) conversionService.convert(map, Map.class)).isSameAs(map);
  }

  @Test
  public void emptyMap() throws Exception {
    Map<String, String> map = new HashMap<>();
    GenericDescriptor sourceType = GenericDescriptor.forObject(map);
    GenericDescriptor targetType = new GenericDescriptor(getClass().getField("emptyMapTarget"));

    assertThat(conversionService.canConvert(sourceType.getType(), targetType)).isTrue();
    final Map<String, String> convert = conversionService.convert(map, targetType);
    assertThat(convert).isSameAs(map);
  }

  @Test
  public void emptyMapNoTargetGenericInfo() throws Exception {
    Map<String, String> map = new HashMap<>();

    assertThat(conversionService.canConvert(Map.class, Map.class)).isTrue();
    assertThat((Map<?, ?>) conversionService.convert(map, Map.class)).isSameAs(map);
  }

  @Test
  public void emptyMapDifferentTargetImplType() throws Exception {
    Map<String, String> map = new HashMap<>();
    GenericDescriptor sourceType = GenericDescriptor.forObject(map);
    GenericDescriptor targetType = new GenericDescriptor(getClass().getField("emptyMapDifferentTarget"));

    assertThat(conversionService.canConvert(sourceType.getType(), targetType)).isTrue();

    LinkedHashMap<String, String> result = conversionService.convert(map, targetType);
    assertThat(result).isEqualTo(map);
    assertThat(result.getClass()).isEqualTo(LinkedHashMap.class);
  }

  @Test
  public void noDefaultConstructorCopyNotRequired() throws Exception {
    // SPR-9284
    NoDefaultConstructorMap<String, Integer> map = new NoDefaultConstructorMap<>(
            Collections.singletonMap("1", 1));

    GenericDescriptor sourceType = GenericDescriptor.map(NoDefaultConstructorMap.class,
                                                         GenericDescriptor.valueOf(String.class),
                                                         GenericDescriptor.valueOf(Integer.class));
    GenericDescriptor targetType = GenericDescriptor.map(NoDefaultConstructorMap.class,
                                                         GenericDescriptor.valueOf(String.class),
                                                         GenericDescriptor.valueOf(Integer.class));

    assertThat(conversionService.canConvert(sourceType.getType(), targetType)).isTrue();

    Map<String, Integer> result = conversionService.convert(map, targetType);
    assertThat(result).isEqualTo(map);
    assertThat(result.getClass()).isEqualTo(NoDefaultConstructorMap.class);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void multiValueMapToMultiValueMap() throws Exception {
    DefaultConversionService.addDefaultConverters(conversionService);
    MultiValueMap<String, Integer> source = new DefaultMultiValueMap<>();
    source.put("a", Arrays.asList(1, 2, 3));
    source.put("b", Arrays.asList(4, 5, 6));
    GenericDescriptor targetType = new GenericDescriptor(getClass().getField("multiValueMapTarget"));

    MultiValueMap<String, String> converted = conversionService.convert(source, targetType);
    assertThat(converted.size()).isEqualTo(2);
    assertThat(converted.get("a")).isEqualTo(Arrays.asList("1", "2", "3"));
    assertThat(converted.get("b")).isEqualTo(Arrays.asList("4", "5", "6"));
  }

  @Test
  public void mapToMultiValueMap() throws Exception {
    DefaultConversionService.addDefaultConverters(conversionService);

    Map<String, Integer> source = new HashMap<>();
    source.put("a", 1);
    source.put("b", 2);
    GenericDescriptor targetType = new GenericDescriptor(getClass().getField("multiValueMapTarget"));

    MultiValueMap<String, String> converted = conversionService.convert(source, targetType);
    assertThat(converted.size()).isEqualTo(2);
    assertThat(converted.get("a")).isEqualTo(Arrays.asList("1"));
    assertThat(converted.get("b")).isEqualTo(Arrays.asList("2"));
  }

  @Test
  public void stringToEnumMap() throws Exception {

    conversionService.addConverters(new StringToEnumConverter());
    conversionService.addConverters(new CollectionToCollectionConverter(conversionService));
    conversionService.addConverters(new IntegerConverter(int.class),
                                    new IntegerConverter(Integer.class));

    Map<String, Integer> source = new HashMap<>();
    source.put("A", 1);
    source.put("C", 2);
    EnumMap<MyEnum, Integer> result = new EnumMap<>(MyEnum.class);
    result.put(MyEnum.A, 1);
    result.put(MyEnum.C, 2);

    final EnumMap<MyEnum, Integer> enumMap = conversionService.convert(source,
                                                                       new GenericDescriptor(getClass().getField("enumMap")));
    assertThat(enumMap)
            .isEqualTo(result);
  }

  public Map<Integer, Integer> scalarMapTarget;

  public Map<Integer, List<Integer>> collectionMapTarget;

  public Map<String, List<String>> sourceCollectionMapTarget;

  public Map<String, String> emptyMapTarget;

  public LinkedHashMap<String, String> emptyMapDifferentTarget;

  public MultiValueMap<String, String> multiValueMapTarget;

  @SuppressWarnings("rawtypes")
  public Map notGenericMapSource;

  public EnumMap<MyEnum, Integer> enumMap;

  @SuppressWarnings("serial")
  public static class NoDefaultConstructorMap<K, V> extends HashMap<K, V> {

    public NoDefaultConstructorMap(Map<? extends K, ? extends V> map) {
      super(map);
    }
  }

  public enum MyEnum {A, B, C}

}
