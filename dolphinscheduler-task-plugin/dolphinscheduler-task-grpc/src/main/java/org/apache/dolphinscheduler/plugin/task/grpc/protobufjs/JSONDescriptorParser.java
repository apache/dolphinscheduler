package org.apache.dolphinscheduler.plugin.task.grpc.protobufjs;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.types.*;
import org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.types.Enum;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

public class JSONDescriptorParser {


    public Descriptors.FileDescriptor buildDescriptor(Root root) throws Descriptors.DescriptorValidationException {
        return parseRoot(root);
    }

    private void parse(String name, ReflectionObject pbObject) {

    }

    private Namespace parseNamespace(Namespace ns) {
        List<String> packageNameNS = new ArrayList<>();
        while (true) {
            if (ns.nested != null && ns.nested.values().size() == 1 && ns.nested.values().toArray()[0] instanceof Namespace && !(ns.nested instanceof Type) && !(ns.nested instanceof Service)) {
                ns = (Namespace) ns.nested.values().toArray()[0];
                packageNameNS.add((String) ns.nested.keySet().toArray()[0]);
            } else {
                break;
            }
        }
        return ns;
    }

    public String readPackageName(Root root) {
        List<String> packageNameNS = new ArrayList<>();
        Namespace ns = root;
        while (true) {
            if (ns.nested != null && ns.nested.values().size() == 1 && ns.nested.values().toArray()[0] instanceof Namespace && !(ns.nested instanceof Type) && !(ns.nested instanceof Service)) {
                packageNameNS.add((String) ns.nested.keySet().toArray()[0]);
                ns = (Namespace) ns.nested.values().toArray()[0];
            } else {
                break;
            }
        }
        return String.join(".", packageNameNS);
    }

    private Descriptors.FileDescriptor parseRoot(Root root) throws Descriptors.DescriptorValidationException {
        DescriptorProtos.FileDescriptorProto.Builder fileDescriptorProtoBuilder =
                DescriptorProtos.FileDescriptorProto.newBuilder()
                        .setPackage(readPackageName(root));
        Namespace innerNS = parseNamespace(root);
        if (innerNS.nested != null) innerNS.nested.forEach((name, pbObject) -> {
            if (pbObject instanceof Namespace) {
                Namespace ns = (Namespace) pbObject;
                if (ns instanceof Type) {
                    fileDescriptorProtoBuilder.addMessageType(parseType(name, (Type) ns));
                } else if (ns instanceof Service) {
                    fileDescriptorProtoBuilder.addService(parseService(name, (Service) ns));
                } else {

                }
            } else if (pbObject instanceof Enum) {
                fileDescriptorProtoBuilder.addEnumType(parseEnum(name, (Enum) pbObject));
            } else if (pbObject instanceof Field) {

            } else if (pbObject instanceof OneOf) {

            } else if (pbObject instanceof Method) {

            }
        });
        Descriptors.FileDescriptor fileDescriptor =
                Descriptors.FileDescriptor.buildFrom(fileDescriptorProtoBuilder.build(), new Descriptors.FileDescriptor[0]);
        return fileDescriptor;
    }

    private DescriptorProtos.DescriptorProto.Builder parseType(String selfName, Type type) {
        DescriptorProtos.DescriptorProto.Builder descriptorProtoBuilder = DescriptorProtos.DescriptorProto.newBuilder()
                .setName(selfName);
        if (type.fields != null) type.fields.forEach((name, pbObject) -> {
            if (pbObject instanceof Field) {
                descriptorProtoBuilder.addField(parseField(name, (Field) pbObject));
            }
        });
        if (type.nested != null) type.nested.forEach((name, pbObject) -> {
            if (pbObject instanceof Enum) {
                descriptorProtoBuilder.addEnumType(parseEnum(name, (Enum) pbObject));
            } else if (pbObject instanceof Type) {
                descriptorProtoBuilder.addNestedType(parseType(name, (Type) pbObject));
            }
        });
        return descriptorProtoBuilder;
    }

    private DescriptorProtos.ServiceDescriptorProto.Builder parseService(String selfName, Service service) {
        DescriptorProtos.ServiceDescriptorProto.Builder serviceDescriptorProtoBuilder = DescriptorProtos.ServiceDescriptorProto.newBuilder()
                .setName(selfName);
        if (service.methods != null) service.methods.forEach((name, pbObject) -> {
            if (pbObject instanceof Method) {
                serviceDescriptorProtoBuilder.addMethod(parseMethod(name, (Method) pbObject));
            }
        });
        return serviceDescriptorProtoBuilder;
    }

    private DescriptorProtos.MethodDescriptorProto.Builder parseMethod(String selfName, Method method) {
        DescriptorProtos.MethodDescriptorProto.Builder methodDescriptorProtoBuilder = DescriptorProtos.MethodDescriptorProto.newBuilder()
                .setName(selfName)
                .setInputType(method.requestType)
                .setOutputType(method.responseType);
        return methodDescriptorProtoBuilder;
    }

    private DescriptorProtos.EnumDescriptorProto.Builder parseEnum(String selfName, Enum enumObj) {
        DescriptorProtos.EnumDescriptorProto.Builder enumDescriptorProtoBuilder = DescriptorProtos.EnumDescriptorProto.newBuilder()
                .setName(selfName);
        enumObj.values.forEach((name, id) -> {
            DescriptorProtos.EnumValueDescriptorProto.Builder enumValueDescriptorProtoBuilder = DescriptorProtos.EnumValueDescriptorProto.newBuilder()
                    .setName(name)
                    .setNumber(id);
            enumDescriptorProtoBuilder.addValue(enumValueDescriptorProtoBuilder);
        });
        return enumDescriptorProtoBuilder;
    }

    private DescriptorProtos.FieldDescriptorProto.Builder parseField(String selfName, Field field) {
        DescriptorProtos.FieldDescriptorProto.Builder fieldDescriptorProtoBuilder = DescriptorProtos.FieldDescriptorProto.newBuilder()
                .setName(selfName)
                .setNumber(field.id);
        try {
            fieldDescriptorProtoBuilder.setType(DescriptorProtos.FieldDescriptorProto.Type.valueOf("TYPE_" + field.type.toUpperCase()));
        } catch (IllegalArgumentException e) {
            fieldDescriptorProtoBuilder.setTypeName(field.type);
        }
        return fieldDescriptorProtoBuilder;
    }

    private DescriptorProtos.FieldDescriptorProto.Builder parseMapField(String selfName, MapField mapField) {
        DescriptorProtos.FieldDescriptorProto.Builder mapFieldDescriptorProtoBuilder = DescriptorProtos.FieldDescriptorProto.newBuilder()
                .setName(selfName);
//                .setType(T);
        throw new NotImplementedException();
//        return mapFieldDescriptorProtoBuilder;
    }

    private DescriptorProtos.OneofDescriptorProto.Builder parseOneof(String selfName, OneOf oneof, Type parent) {
        DescriptorProtos.OneofDescriptorProto.Builder oneofDescriptorProtoBuilder = DescriptorProtos.OneofDescriptorProto.newBuilder()
                .setName(selfName);
        throw new NotImplementedException();
//        return oneofDescriptorProtoBuilder;
    }
}
